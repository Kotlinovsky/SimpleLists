package io.kotlinovsky.simplelists.model

/**
 * ViewObject базового элемента списка
 *
 * ID - тип ID
 * T - указать тип, который реализуете.
 */
interface BasicListViewObject<ID, T> {

    /**
     * Возвращает ID объекта/записи
     * Не должен зависить от содержимого обьекта!
     *
     * @return ID записи/обьекта.
     */
    fun getId(): ID

    /**
     * Обнаруживает изменения и возвращает их флаги
     *
     * @param vo Предыдущий ViewObject (до внесения изменений)
     * @return Список с флагами изменений
     */
    fun getChangePayload(vo: T): List<Any>? {
        return null
    }
}