 (ns addressbook.core-test
  (:require [clojure.test :refer :all]
            [addressbook.core :refer :all]
            [ring.mock.request :as mock]))




(deftest get-values-test
  (is (= (get-values :id (mock/request :get "2"))
         {:status  404
          :headers {"content-type" "text/plain"}
          :body    "Requested record not found!"})))

;;id is a string not a number


(deftest delete-by-id-test
  (is (= (delete-by-id (mock/request :delete "2"))
         {:status  404
          :headers {"content-type" "text/plain"}
          :body    "Requested record does not exist!"})))


(deftest enter-data-test
  (let [resp (enter-data (mock/request :post "/address/"  "{ \"name\" : \"rub\",
 \"email\" : \"rub@helpshift.com\",
 \"phone\" : \"1656033225\",
 \"address\" : {
   \"house\" : \"420\",
   \"apartment\" : \"Bellview Complex\",
   \"city\" : \"San Francisco\",
   \"state\" : \"CA\",
   \"zip\" : \"94107\"
 }
}"))]
    (is (= 201
           (:status resp)))
    (is (= {"content-type" "text/plain"}
           (:headers resp)))
    (is (.startsWith (:body resp)
                     "New record entered with ID: " ))))



