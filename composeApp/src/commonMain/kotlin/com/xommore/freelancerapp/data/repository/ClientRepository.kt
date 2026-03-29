package com.xommore.freelancerapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.db.FreelancerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClientRepository(private val db: FreelancerDatabase) {

    private val queries = db.freelancerDatabaseQueries

    fun getClients(userId: String): Flow<List<Client>> {
        return queries.getClientsByUser(userId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toClient() } }
    }

    fun getClientById(clientId: String): Client? {
        return queries.getClientById(clientId).executeAsOneOrNull()?.toClient()
    }

    fun insertClient(client: Client) {
        queries.insertClient(
            id = client.id,
            userId = client.userId,
            company = client.company,
            name = client.name,
            email = client.email,
            phone = client.phone,
            memo = client.memo,
            createdAt = client.createdAt
        )
    }

    fun updateClient(client: Client) {
        queries.updateClient(
            company = client.company,
            name = client.name,
            email = client.email,
            phone = client.phone,
            memo = client.memo,
            id = client.id
        )
    }

    fun deleteClient(clientId: String) {
        queries.deleteClientById(clientId)
    }

    fun getClientCount(userId: String): Long {
        return queries.getClientCount(userId).executeAsOne()
    }
}

private fun com.xommore.freelancerapp.db.Clients.toClient(): Client {
    return Client(
        id = id,
        userId = userId,
        company = company,
        name = name,
        email = email,
        phone = phone,
        memo = memo,
        createdAt = createdAt
    )
}