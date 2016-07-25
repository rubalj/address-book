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
                             (s/required-key :zip) zip-regex}})


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
  "Returns the record if the value argument exists for the key"
  [k v]
  (let [a (some (fn [x] (if (= (.toLowerCase v) (.toLowerCase (get x k))) x ))@BOOK)]a))


(defn exists?
  "Checks if the key value pair already exists in the addressbook"
  [k w]
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
      {:status 412
       :headers {"content-type" "text/plain"}
       :body (str "Value exists")}
      (do (swap! BOOK conj body)
          {:status 201
           :headers {"content-type" "text/plain"}
           :body (str "New record entered with ID: " id)}))))



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
   :headers {"content-type" "application/json"}
   :body (json/generate-string {:all @BOOK})})


(defn get-values
  "Display values of a record based on the key and value provided. 
  If the value is a name then the first name and last name must be separted by %20"
  [k v]
  (let [a (some (fn [x] (if (= (.toLowerCase v) (.toLowerCase (get x k))) x)) @BOOK)]
    (if a
      {:status 200
       :headers {"content-type " "application/json"}
       :body (json/generate-string a {:pretty true})}
      {:status 404
       :headers {"content-type" "text/plain"}
       :body (str "Requested record not found!")})))


(defn process-request-handler
  "Returns the body of the request in the form of a map."
  [request]
  (process-body request))


(defn update-by-id
  "Updates the record represented by the id provided as an argument."
  [handler id]
  (fn [request]
    (if (= -1 (get-index id))
      {:status 404
       :headers {"content-type" "text/plain"}
       :body (str "No record with ID " id " found!")}
      (do
        (let [response (merge {:id id} (process-request-handler request))]
          (if (or (exists? :name (get response  :name)) (exists? :email (get response :email)))
            {:status 412
             :headers {"content-type" "text/plain"}
             :body (str "Value exists")}
            {:status 200
             :headers {"content-type" "text/plain"}
             :body (do (reset! BOOK (assoc @BOOK (get-index id) response ))
                       (str "ID: " id " updated successfully!"))}))))))


(defn delete-by-id
  "Deletes the record represented by the id provided as an argument."
  [id]
  (let [index (get-index id)]
    (if (not  (= -1 (get-index id)))
      (do (reset! BOOK (vec (concat (subvec @BOOK 0 index) (subvec @BOOK (inc index)))))
          {:status 200
           :headers {"content-type " "text/plain"}
           :body (str "ID: " id " deleted!")})
      {:status 404
       :headers {"content-type" "text/plain"}
       :body (str "Requested record does not exist!")})))


(defroutes address-book
  (GET "/address" [] display-records)  
  (GET "/address/:id" [id] (get-values :id id))      
  (GET "/address/search/:name" [name] (get-values :name name))
  (POST "/address" [] enter-data)           
  (PUT "/address/:id" [id] (update-by-id process-request-handler id))
  (DELETE "/address/:id" [id] (delete-by-id id)))  


