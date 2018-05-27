(ns robertluo.lean-schema
  (:require
   [datomic.api :as d]))

(defn- parse-opt
  [opt]
  (condp #(%1 %2) opt
    #{:string :boolean :long :bigint :float
      :double :bigdec :ref :instant :uuid :uri :bytes}
    [:db/valueType (keyword "db.type" (name opt))]
    #{:one :many}
    [:db/cardinality (keyword "db.cardinality" (name opt))]
    #{:value :identity}
    [:db/unique (keyword "db.unique" (name opt))]
    #{:index :fulltext :isComponent :noHistory}
    [(keyword "db" (name opt)) true]
    :else (throw (ex-info "Invalid opt" {:opt opt}))))

(comment
  (parse-opt :index)
  (parse-opt :long)
  )

(defn attr
  [ident doc & opts]
  (let [opts (->> opts (map parse-opt) (into {}))]
    (merge
     {:db/ident       ident
      :db/doc         doc
      :db/cardinality :db.cardinality/one}
     opts)))

(defn mk-schema
  "使用简写数据定义 datomic 属性，返回它的标准格式。
   标准格式定义见 http://docs.datomic.com/schema.html

   - `:attrs` 定义属性，[属性ident 文档字符串 其他限定*]+

     - `ident` 属性名, 必须是qualified keyward
     - `doc` 文档字符串
     - 其余属性由任意数量的关键字组成, 次序无关
     - `:string`, `:long`, `:uuid`, `:boolean` 等是属性类型，**必须**
     - `:one`, `:many` 是属性 1 或 n 的数量关系，默认为 `:one`
     - `:value`, `:identity` 是说明唯一性, 默认为 `nil`
     - `:index`, `:fulltext`, `:isComponent`, `:noHistory` 是开关属性，默认都是 `false`

   - `:idents` 定义枚举，枚举ident*
   - `:functions` 定义数据库函数 [函数ident 文档字符串 函数实现(用d/function)]"
  [{:keys [attrs idents functions]}]
  (concat (mapv #(apply attr %) attrs)
          (mapv #(hash-map :db/ident %) idents)
          (mapv (fn [[id doc f]] {:db/ident id :db/doc doc :db/fn f})
                functions)))
