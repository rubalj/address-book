(ns addressbook.core
  (:require [ring.adapter.jetty :as ring]
            [cheshire.core :as json])
 (:use compojure.core))


(defonce BOOK (atom []))  ;;taking vector to maintain the id order


(defonce id (atom 0))


(defn process-body
  "The function accepts the body of request as an argument. 
   This function converts the input request from JSON into a clojure map and returns this map."
  [request]
  (json/parse-string (slurp (:body request)) true))


(defn enter-data
  "This function takes a map as an argument and adds a unique ID to it. 
   It enters this processed argument into the database list and returns the ID of the record."
  [request]
  (swap! id inc)
  (let [body (merge {:id @id} (process-body request))]
    (swap! BOOK (fn [m] (merge m body)))
    (str "ID of the record: " @id)))


;;format this
(defn display-data-handler
  "A handler to display all the records present in the address book"
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (json/generate-string @BOOK {:pretty true})})



;;format this
(defn display
  "A handler to display all the records present in the address book"
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (map (fn [x] (json/generate-string x {:pretty true})) @BOOK)})


(defn get-values
  "Display values of a record based on the key and value provided.  "
  [k v]
  (let [a (some (fn [x] (if (= v (get x k)) x)) @BOOK)]
    (json/generate-string a {:pretty true})))

(defn get-by-id
  [id]
  (let [a (some (fn [x] (if (= id (get x :id)) x)) @BOOK)] a))


(defn get-index
  [id]
  (.indexOf @BOOK (get-by-id (Integer. id))) )


(defn delete-by-index
  [id]
  (let [index (get-index id)]
    (vec (concat (subvec @BOOK 0 index) (subvec @BOOK (inc index))))))


(defn tryone [id]
  (reset! BOOK (delete-by-index id))
  (str "ID " id " deleted "))


;;change the name
(defroutes address
  (GET "/address" [] display)  ;;working
  (GET "/address/:id" [id] (get-values :id (Integer. id))) ;;working     
  (GET "/address/search/:name" [name] (get-values :name name)) ;;working
  (POST "/address" [] enter-data)           ;;working
  (PUT "/address" [] update-by-id)
  (DELETE "/address/:id" [id] (tryone id)))  ;;working


