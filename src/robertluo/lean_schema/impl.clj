(ns robertluo.lean-schema.impl)

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

(defn attr
  [ident doc & opts]
  (let [opts (->> opts (map parse-opt) (into {}))]
    (merge
     {:db/ident       ident
      :db/doc         doc
      :db/cardinality :db.cardinality/one}
     opts)))
