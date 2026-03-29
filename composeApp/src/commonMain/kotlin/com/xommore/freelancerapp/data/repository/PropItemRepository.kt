package com.xommore.freelancerapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.xommore.freelancerapp.data.model.PropItem
import com.xommore.freelancerapp.db.FreelancerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PropItemRepository(private val db: FreelancerDatabase) {

    private val queries = db.freelancerDatabaseQueries

    fun getPropsForProject(projectId: String): Flow<List<PropItem>> {
        return queries.getPropsForProject(projectId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toPropItem() } }
    }

    fun getPropsForProjectOnce(projectId: String): List<PropItem> {
        return queries.getPropsForProject(projectId)
            .executeAsList()
            .map { it.toPropItem() }
    }

    fun insertPropItem(propItem: PropItem) {
        queries.insertPropItem(
            id = propItem.id,
            projectId = propItem.projectId,
            name = propItem.name,
            amount = propItem.amount,
            receiptUri = propItem.receiptUri,
            memo = propItem.memo,
            createdAt = propItem.createdAt
        )
    }

    fun insertPropItems(propItems: List<PropItem>) {
        db.transaction {
            propItems.forEach { insertPropItem(it) }
        }
    }

    fun deletePropItemById(propId: String) {
        queries.deletePropItemById(propId)
    }

    fun deleteAllPropsForProject(projectId: String) {
        queries.deleteAllPropsForProject(projectId)
    }
}

private fun com.xommore.freelancerapp.db.Prop_items.toPropItem(): PropItem {
    return PropItem(
        id = id,
        projectId = projectId,
        name = name,
        amount = amount,
        receiptUri = receiptUri,
        memo = memo,
        createdAt = createdAt
    )
}