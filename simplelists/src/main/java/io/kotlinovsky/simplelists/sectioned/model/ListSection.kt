package io.kotlinovsky.simplelists.sectioned.model

/**
 * Секция списка
 *
 * @param isLoading Включена загрузка?
 * @param startPosition Стартовая позиция секции
 * @param itemsCount Количество элементов в секции
 */
data class ListSection(
    var isLoading: Boolean,
    var startPosition: Int,
    var itemsCount: Int
)