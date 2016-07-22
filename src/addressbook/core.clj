(ns addressbook.core
  (:require [ring.adapter.jetty :as ring]
            [cheshire.core :as json]
            [schema.core :as s :include-macros true])
  (:use compojure.core)
  (:import java.util.UUID))


(defonce BOOK (atom []))  


(def email-regex #".+\@.+\..+")
(def phn-regex #"\d\d\d\d\d\d\d\d\d\d")
(def zip-regex #"\d\d\d\d\d")


(def data-schema
 {(s/required-key :name) s/Str
  (s/required-key :email) email-regex
  (s/optional-key :phone) phn-regex
  (s/optional-key :address) {(s/required-key :house) s/Str
                             (s/required-key :apartment) s/Str
                             (s/required-key :city) s/Str
                             (s/required-key :state) s/Str
                             (s/required-key :zip) zip-regex
                             }})



(defn process-body
  "Accepts the body of request as an argument and converts the input 
  request from JSON into a clojure map and returns this map."
  [request]
  (s/validate data-schema (json/parse-string (slurp (:body request)) true)))


(defn gen-id
  "Returns a unique id."
  []
  (str (UUID/randomUUID)))

(defn get-values-as-map
  [k v]
  (let [a (some (fn [x] (if (= (.toLowerCase v) (.toLowerCase (get x k))) x ))@BOOK)]a))


(defn exists? [k w]
  (if (get-values-as-map k w )
    true
    false))

(defn enter-data
  "Takes a map as an argument and adds a unique ID to it and enters it 
  into the addressbook and returns the ID of the record."
  [request]
  (let [id (gen-id)
        body (merge {:id id} (process-body request))]
    (if (or (exists? :name (get body :name)) (exists? :email (get body :email)))
      (str "Value exists!")
      (swap! BOOK (fn [m] (merge m body))))))





(defn get-by-id
  "Returns a record map matching the id."
  [id]
  (let [a (some (fn [x] (if (= id (get x :id)) x)) @BOOK)] a))


(defn get-index
  "Returns the index of the record in the addressbook based on the id in the argument."
  [id]
  (.indexOf @BOOK (get-by-id id)) )


(defn display-records
  "Displays all the records present in the address book"
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (map (fn [x] (json/generate-string x {:pretty true})) @BOOK)})


(defn get-values
  "Display values of a record based on the key and value provided. 
  If the value is a name then the first name and last name must be separted by %20"
  [k v]
  (let [a (some (fn [x] (if (= (.toLowerCase v) (.toLowerCase (get x k))) x)) @BOOK)]
    (json/generate-string a {:pretty true})))


(defn process-request-handler
  "Returns the body of the request in the form of a map."
  [request]
  (process-body request))


(defn update-by-id
  "Updates the record represented by the id provided as an argument."
  [handler id]
  (fn [request]
    (let [response (merge {:id id} (process-request-handler request))]
      (reset! BOOK (assoc @BOOK (get-index id) response ))
      (str "ID: " id " updated successfully!"))))


(defn delete-by-id
  "Deletes the record represented by the id provided as an argument."
  [id]
  (let [index (get-index id)]
    (reset! BOOK (vec (concat (subvec @BOOK 0 index) (subvec @BOOK (inc index)))))
    (str "ID: " id " deleted!")))


(defroutes address-book
  (GET "/address" [] display-records)  
  (GET "/address/:id" [id] (get-values :id id))      
  (GET "/address/search/:name" [name] (get-values :name name))
  (POST "/address" [] enter-data)           
  (PUT "/address/:id" [id] (update-by-id process-request-handler id))
  (DELETE "/address/:id" [id] (delete-by-id id)))  


