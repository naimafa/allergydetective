package com.nfar.allergydetective.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.CategoryInfo
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.IngredientSearch
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.PropertyInfo
import com.nfar.allergydetective.domain.diary.SearchEntry
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.NamePrefix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomIngredientState(
    val name: String = "",
    val categories: List<CategoryInfo> = emptyList(),
    val categoryId: Long? = null,
    val properties: List<PropertyInfo> = emptyList(),
    val propertyIds: Set<Long> = emptySet(),
    val baseQuery: String = "",
    val baseResults: List<IngredientInfo> = emptyList(),
    val base: IngredientInfo? = null,
    val saving: Boolean = false,
    /** Set once the ingredient exists, the screen then closes the dialog and selects it. */
    val created: IngredientInfo? = null,
) {
    val canCreate: Boolean get() = name.isNotBlank() && categoryId != null && !saving
}

/** "New ingredient" dialog (CONTENT_Zutaten: name + category + "similar to …"). */
class CustomIngredientViewModel(
    private val repository: CustomIngredientRepository,
    private val meals: MealRepository,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomIngredientState())
    val state: StateFlow<CustomIngredientState> = _state.asStateFlow()

    private var ingredientsById: Map<Long, IngredientInfo> = emptyMap()
    private var search = IngredientSearch(emptyList())

    init {
        viewModelScope.launch {
            val ingredients = meals.ingredients()
            ingredientsById = ingredients.associateBy { it.id }
            val synonyms = meals.synonyms(language.language())
            search = IngredientSearch(
                ingredients.map { ingredient ->
                    val name = ingredient.key?.let { names.name(NamePrefix.INGREDIENT, it) } ?: ingredient.customName.orEmpty()
                    SearchEntry(ingredient.id, name, synonyms[ingredient.id].orEmpty())
                },
            )
            val categories = repository.categories()
            val properties = repository.properties()
            _state.update { it.copy(categories = categories, properties = properties) }
        }
    }

    /** Starts a new dialog with [name], e.g. the search text that found nothing. */
    fun start(name: String) {
        _state.update {
            CustomIngredientState(name = name.trim(), categories = it.categories, properties = it.properties)
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }

    fun onCategoryChange(categoryId: Long) = _state.update { it.copy(categoryId = categoryId) }

    fun onPropertyToggle(propertyId: Long) = _state.update {
        it.copy(propertyIds = if (propertyId in it.propertyIds) it.propertyIds - propertyId else it.propertyIds + propertyId)
    }

    fun onBaseQueryChange(query: String) = _state.update {
        it.copy(baseQuery = query, baseResults = search.search(query, limit = BASE_RESULTS).mapNotNull(ingredientsById::get))
    }

    /** "Similar to …": suggests category and triggers of the base, both stay editable. */
    fun onBaseSelected(base: IngredientInfo) {
        viewModelScope.launch {
            val properties = repository.propertiesOf(base.id)
            val category = repository.categoryOf(base.id)
            _state.update {
                it.copy(base = base, baseQuery = "", baseResults = emptyList(), propertyIds = properties, categoryId = category ?: it.categoryId)
            }
        }
    }

    fun onCreate() {
        val current = _state.value
        val categoryId = current.categoryId
        if (!current.canCreate || categoryId == null) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val created = repository.create(current.name.trim(), categoryId, current.base?.id, current.propertyIds)
            _state.update { it.copy(saving = false, created = created) }
        }
    }

    fun onCreatedHandled() = _state.update { it.copy(created = null) }

    private companion object {
        const val BASE_RESULTS = 5
    }
}
