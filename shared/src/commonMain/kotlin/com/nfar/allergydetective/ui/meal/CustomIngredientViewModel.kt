package com.nfar.allergydetective.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.CategoryInfo
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.PropertyInfo
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomIngredientState(
    val name: String = "",
    /** Known ingredients it is made of, e.g. spelt flour, oat flakes, linseed. */
    val components: List<IngredientInfo> = emptyList(),
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    /** Taken from the first component; the user only picks one when there are no components. */
    val derivedCategoryId: Long? = null,
    val chosenCategoryId: Long? = null,
    /** Union of the components' triggers, shown for information only. */
    val propertyIds: Set<Long> = emptySet(),
    val categories: List<CategoryInfo> = emptyList(),
    val properties: List<PropertyInfo> = emptyList(),
    /** An ingredient with the same name or synonym exists: creating is blocked, it can be used instead. */
    val duplicate: IngredientInfo? = null,
    /** A similar ingredient exists (typo, part of a name): on create the user is asked once. */
    val similar: IngredientInfo? = null,
    /** The user saw [similar] and still wants a new ingredient. */
    val askSimilar: Boolean = false,
    val saving: Boolean = false,
    /** Set once the ingredient exists, the screen then closes the dialog and selects it. */
    val created: IngredientInfo? = null,
) {
    val categoryId: Long? get() = derivedCategoryId ?: chosenCategoryId
    val canCreate: Boolean get() = name.isNotBlank() && categoryId != null && duplicate == null && !saving
}

/**
 * "New ingredient" (self-test 18.11.): the user only says what it contains.
 * Category and triggers come from the chosen ingredients, nobody has to know allergens.
 */
class CustomIngredientViewModel(
    private val repository: CustomIngredientRepository,
    private val meals: MealRepository,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomIngredientState())
    val state: StateFlow<CustomIngredientState> = _state.asStateFlow()

    private var catalog = IngredientCatalog.EMPTY

    init {
        viewModelScope.launch {
            catalog = IngredientCatalog.load(meals, names, language)
            val categories = repository.categories()
            val properties = repository.properties()
            _state.update { it.copy(categories = categories, properties = properties) }
        }
    }

    /** Starts a new dialog with [name], e.g. the search text that found nothing. */
    fun start(name: String) {
        _state.update {
            CustomIngredientState(categories = it.categories, properties = it.properties).withNameCheck(name.trim())
        }
    }

    fun onNameChange(name: String) = _state.update { it.withNameCheck(name) }

    /**
     * Self-test 18.11.: no junk in the database. The same name or a synonym blocks creating,
     * a similar one (typo, part of a name) is offered before a new ingredient is made.
     */
    private fun CustomIngredientState.withNameCheck(name: String): CustomIngredientState {
        val match = name.takeIf { it.isNotBlank() }?.let(catalog::bestMatch)
        return copy(
            name = name,
            duplicate = match?.takeIf { it.second == 0 }?.first,
            similar = match?.takeIf { it.second > 0 }?.first,
            askSimilar = false,
        )
    }

    /** Use the existing or similar ingredient instead of creating a new one. */
    fun onUseExisting() {
        val existing = _state.value.duplicate ?: _state.value.similar ?: return
        _state.update { it.copy(created = existing) }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query, results = catalog.search(query)) }

    fun onAddComponent(ingredient: IngredientInfo) {
        if (ingredient in _state.value.components) return
        updateComponents(_state.value.components + ingredient)
    }

    fun onRemoveComponent(ingredient: IngredientInfo) = updateComponents(_state.value.components - ingredient)

    fun onCategoryChange(categoryId: Long) = _state.update { it.copy(chosenCategoryId = categoryId) }

    private fun updateComponents(components: List<IngredientInfo>) {
        _state.update { it.copy(components = components, query = "", results = emptyList()) }
        viewModelScope.launch {
            val properties = components.flatMap { repository.propertiesOf(it.id) }.toSet()
            val category = components.firstOrNull()?.let { repository.categoryOf(it.id) }
            _state.update {
                // Only apply if the components did not change in the meantime.
                if (it.components != components) it else it.copy(propertyIds = properties, derivedCategoryId = category)
            }
        }
    }

    fun onCreate() {
        val current = _state.value
        val categoryId = current.categoryId
        if (!current.canCreate || categoryId == null) return
        // Similar ingredient: ask once, a second tap on create confirms.
        if (current.similar != null && !current.askSimilar) {
            _state.update { it.copy(askSimilar = true) }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val created = repository.create(
                name = current.name.trim(),
                categoryId = categoryId,
                baseIngredientId = current.components.firstOrNull()?.id,
                propertyIds = current.propertyIds,
            )
            _state.update { it.copy(saving = false, created = created) }
        }
    }

    fun onCreatedHandled() = _state.update { it.copy(created = null) }
}
