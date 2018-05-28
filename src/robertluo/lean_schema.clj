(ns robertluo.lean-schema
  (:require
   [datomic.api :as d]
   [robertluo.lean-schema.impl :as impl]))

(defn attrs
  "使用简写数据定义 datomic 属性，返回它的标准格式。
   标准格式定义见 http://docs.datomic.com/schema.html

     - `ident` 属性名, 必须是qualified keyward
     - `doc` 文档字符串
     - 其余属性由任意数量的关键字组成, 次序无关
     - `:string`, `:long`, `:uuid`, `:boolean` 等是属性类型，**必须**
     - `:one`, `:many` 是属性 1 或 n 的数量关系，默认为 `:one`
     - `:value`, `:identity` 说明唯一性, 默认为 `nil`
     - `:index`, `:fulltext`, `:isComponent`, `:noHistory` 开关属性，默认都是 `false`"
  [& attrs]
  (mapv #(apply impl/attr %) attrs))

(defn temp-uri []
  (str "datomic:mem:" (java.util.UUID/randomUUID)))

(defn ensure-db
  "返回一个 datomic 连接在 uri"
  [{:keys [schema uri init-data create?]
    :or {uri (temp-uri)
         create? true}}]
  (and create? (d/create-database uri) (println "Database created."))
  (let [conn (d/connect uri)]
    @(d/transact conn schema)
    (or init-data @(d/transact conn init-data))
    conn))

(comment
  (def schema
    (attrs
     [:greet/name "greet name" :string :identity]))
  (ensure-db {:schema schema}))
