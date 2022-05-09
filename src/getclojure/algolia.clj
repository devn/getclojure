(ns getclojure.algolia
  (:require
   [outpace.config :refer [defconfig]])
  (:import
   (com.algolia.search SearchClient SearchIndex DefaultSearchClient)
   (com.algolia.search.models.indexing SearchResult Query)))

(defconfig algolia-app-id)
(defconfig algolia-admin-api-key)
(defconfig algolia-index)

(def ^:private search-client
  (delay (DefaultSearchClient/create algolia-app-id algolia-admin-api-key)))

(def ^:private search-index
  (delay (.initIndex ^SearchClient @search-client "getclojure_production")))

(defn search
  ([q] (search q 0))
  ([q page-num]
   (let [res (.search ^SearchIndex @search-index (.. (Query. q)
                                                     (setAttributesToRetrieve ["formatted-input"
                                                                               "formatted-output"
                                                                               "formatted-value"])
                                                     (setHitsPerPage (int 25))
                                                     (setPage (int page-num))))]
     {:hits       (.getHits ^SearchResult res)
      :total-hits (.getNbHits ^SearchResult res)
      :pages      (.getNbPages ^SearchResult res)})))
