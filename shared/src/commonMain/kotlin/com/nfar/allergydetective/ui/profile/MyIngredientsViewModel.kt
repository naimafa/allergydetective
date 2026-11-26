package com.nfar.allergydetective.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.CategoryInfo
import com.nfar.allergydetective.domain.diary.CustomIngredient
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.PropertyInfo
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.meal.IngredientCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Editing one own ingredient: name, kind (picture) and what it contains. */
data class IngredientEdit(
    val id: Long,
    val name: String,
    val categoryId: Long,
    val propertyIds: Set<Long>,
    /** Search for an ingredient that is in it; its contents are added. */
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    /** Another ingredient already has this name: saving is blocked. */
    val duplicate: IngredientInfo? = null,
) {
    val canSave: Boolean get() = name.isNotBlank() && duplicate == null
}

/** "Wodurch ersetzen?": a used ingredient is not deleted but replaced, so nothing logged is lost. */
data class IngredientReplace(
    val ingredient: CustomIngredient,
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    val target: IngredientInfo? = null,
)

data class MyIngredientsState(
    val loading: Boolean = true,
    val items: List<CustomIngredient> = emptyList(),
    val categories: List<CategoryInfo> = emptyList(),
    val properties: List<PropertyInfo> = emptyList(),
    val editing: IngredientEdit? = null,
    val replacing: IngredientReplace? = null,
    /** An unused ingredient waiting for "really delete?". */
    val deleting: CustomIngredient? = null,
)

/**
 * "Meine Zutaten" (user 25.11.2026): own ingredients can be corrected at any time. Deleting is safe: an unused one
 * goes after one question, a used one is replaced by another ingredient, and its meals and dishes move along.
 */
class MyIngredientsViewModel(
    private val repository: CustomIngredientRepository,
    private val meals: MealRepository,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(MyIngredientsState())
    val state: StateFlow<MyIngredientsState> = _state.asStateFlow()

    private var catalog = IngredientCatalog.EMPTY

    init {
        viewModelScope.launch {
            val categories = repository.categories()
            val properties = repository.properties()
            _state.update { it.copy(categories = categories, properties = properties) }
            reload()
        }
    }

    private suspend fun reload() {
        catalog = IngredientCatalog.load(meals, names, language)
        val items = repository.customIngredients()
        _state.update { it.copy(loading = false, items = items) }
    }

    fun onEdit(item: CustomIngredient) =
        _state.update { it.copy(editing = IngredientEdit(item.id, item.name, item.categoryId, item.propertyIds)) }

    fun onEditName(name: String) = updateEdit { edit ->
        val match = name.trim().takeIf { it.isNotBlank() }?.let(catalog::bestMatch)
        edit.copy(name = name, duplicate = match?.takeIf { (found, score) -> score == 0 && found.id != edit.id }?.first)
    }

    fun onEditCategory(categoryId: Long) = updateEdit { it.copy(categoryId = categoryId) }

    fun onRemoveProperty(propertyId: Long) = updateEdit { it.copy(propertyIds = it.propertyIds - propertyId) }

    fun onEditQuery(query: String) = updateEdit { edit ->
        edit.copy(query = query, results = catalog.search(query).filter { it.id != edit.id })
    }

    /** An ingredient that is in it: its contents are added (as when creating). */
    fun onAddContents(ingredient: IngredientInfo) {
        viewModelScope.launch {
            val added = repository.propertiesOf(ingredient.id)
            updateEdit { it.copy(propertyIds = it.propertyIds + added, query = "", results = emptyList()) }
        }
    }

    fun onCancelEdit() = _state.update { it.copy(editing = null) }

    fun onSaveEdit() {
        val edit = _state.value.editing ?: return
        if (!edit.canSave) return
        _state.update { it.copy(editing = null) }
        viewModelScope.launch {
            repository.update(edit.id, edit.name.trim(), edit.categoryId, edit.propertyIds)
            reload()
        }
    }

    /** Unused: ask once and delete. Used: offer to replace it instead. */
    fun onDelete(item: CustomIngredient) = _state.update {
        if (item.used) it.copy(replacing = IngredientReplace(item)) else it.copy(deleting = item)
    }

    fun onCancelDelete() = _state.update { it.copy(deleting = null) }

    fun onConfirmDelete() {
        val item = _state.value.deleting ?: return
        _state.update { it.copy(deleting = null) }
        viewModelScope.launch {
            // Used in the meantime (another screen): replace instead.
            if (!repository.deleteIfUnused(item.id)) _state.update { it.copy(replacing = IngredientReplace(item)) }
            reload()
        }
    }

    fun onReplaceQuery(query: String) = updateReplace { replace ->
        replace.copy(query = query, results = catalog.search(query).filter { it.id != replace.ingredient.id })
    }

    fun onReplaceTarget(target: IngredientInfo) = updateReplace { it.copy(target = target, query = "", results = emptyList()) }

    fun onCancelReplace() = _state.update { it.copy(replacing = null) }

    fun onConfirmReplace() {
        val replace = _state.value.replacing ?: return
        val target = replace.target ?: return
        _state.update { it.copy(replacing = null) }
        viewModelScope.launch {
            repository.replace(replace.ingredient.id, target.id)
            reload()
        }
    }

    private fun updateEdit(change: (IngredientEdit) -> IngredientEdit) =
        _state.update { state -> state.editing?.let { state.copy(editing = change(it)) } ?: state }

    private fun updateReplace(change: (IngredientReplace) -> IngredientReplace) =
        _state.update { state -> state.replacing?.let { state.copy(replacing = change(it)) } ?: state }
}
