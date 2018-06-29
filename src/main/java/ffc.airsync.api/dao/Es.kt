/*
 * Copyright (c) 2018 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ffc.airsync.api.dao

import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.xcontent.XContentType

fun TransportClient.insertUser(index: String, type: String, id: String, json: String): IndexResponse {
    return this
            .prepareIndex(index, type, id)
            .setSource(json, XContentType.JSON)
            .get()
}

fun TransportClient.get(index: String, type: String, id: String): GetResponse {
    return this
            .prepareGet(index, type, id)
            .get()
}

fun TransportClient.delete(index: String, type: String, id: String): DeleteResponse {
    return this
            .prepareDelete(index, type, id)
            .get()
}

fun TransportClient.search(index: String, type: String): SearchResponse {
    return this
            .prepareSearch(index)
            .setTypes(type)
            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).get()
}
