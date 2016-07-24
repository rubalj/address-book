(ns addressbook.core-test
  (:require [clojure.test :refer :all]
            [addressbook.core :refer :all]
            [ring.mock.request :as mock]))




(deftest get-values-test
  (is (= (get-values :id (mock/request :get "2"))
         {:status  404
          :headers {"content-type" "text/plain"}
          :body    "Requested record not found!"})))


(deftest delete-by-id-test
  (is (= (delete-by-id (mock/request :delete "2"))
         {:status  404
          :headers {"content-type" "text/plain"}
          :body    "Requested record does not exist!"})))










