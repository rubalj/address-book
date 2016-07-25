(defproject addressbook "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
		[ring "1.5.0"]
		[cheshire "5.6.3"]
                [compojure "1.5.1"]
                [ring/ring-mock "0.3.0"]
		[prismatic/schema "1.1.2"]]
  :main ^:skip-aot addressbook.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
