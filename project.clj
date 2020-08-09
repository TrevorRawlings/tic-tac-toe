(defproject tic-tac-toe "0.1.0-SNAPSHOT"
  :description "Web based tic-tac-toe"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[environ "1.2.0"]
                 [honeysql "0.9.10"]
                 [org.clojure/clojure "1.10.1"]
                 [org.postgresql/postgresql "42.2.11"]
                 [ring/ring-core "1.8.1"]
                 [ring/ring-jetty-adapter "1.6.3"]]
  :main ^:skip-aot tic-tac-toe.core
  :target-path "target/%s"

  :plugins [[com.github.metaphor/lein-flyway "6.0.0"]]

  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev     {:plugins [[jonase/eastwood "0.3.10"]
                                 [lein-cljfmt "0.6.8"]]}
             }

  :aliases {}

  :flyway {;; Database connection
           :driver "org.postgresql.Driver"
           :url #=(eval (or (System/getenv "db-url") "jdbc:postgresql://localhost:5432/tic_tac_toe"))
           :user #=(eval (or (System/getenv "db-username") "postgres"))
           :password #=(eval (or (System/getenv "db-password") "password"))
           ;; Migration locations
           :locations ["classpath:flyway/db/migration"]
           :table "migrations"
           ;; Baseline
           :baseline-on-migrate true
           :out-of-order false
           :validate-on-migrate true})
