package com.bidaappscoreboard.store

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.bidaappscoreboard.MetaDataQuery
import com.bidaappscoreboard.model.MetadataGeneral
import com.bidaappscoreboard.service.apolloClient
import com.google.gson.Gson

object Metadata {
    private const val TAG: String = "Metadata"

    var metaGeneral : MetadataGeneral? = null

    suspend fun getMetaData() {
        val response : ApolloResponse<MetaDataQuery.Data> = try {
            apolloClient?.query(MetaDataQuery())!!.execute()
        } catch (e: ApolloException) {
            e.printStackTrace()
            return
        }

        val gMeta : MetadataGeneral = Gson().fromJson(response.data?.MetaDataSB?.general, MetadataGeneral::class.java)

        metaGeneral = gMeta

        return
    }
}