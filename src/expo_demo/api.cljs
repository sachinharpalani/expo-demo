(ns expo-demo.api)

(defn http-post
  ([action data on-success headers]
   (http-post action data on-success headers

              nil))
  ([action data on-success headers on-error]
   #_(js/alert (js->clj (clj->js {:method "POST"
                                :headers (merge {:accept "application/json"
                                                 :content-type "application/json"}
                                                headers)
                                :body (.stringify js/JSON (clj->js data))})))
   (-> (.fetch js/window
               (str action)
               (clj->js {:method "POST"
                         :headers (merge {:accept "application/json"
                                          :content-type "application/json"}
                                         headers)
                         :body (.stringify js/JSON (clj->js data))}))
       (.then (fn [response]
                (.text response)))
       (.then (fn [text]
                (let [json (.parse js/JSON text)
                      obj (js->clj json :keywordize-keys true)]
                  (on-success obj))))
       (.catch (or on-error
                   (fn [error]
                     (js/alert "Error" (str error))))))))

(defn http-get
  ([url on-success on-error]
   (http-get url nil on-success on-error))
  ([url valid-response? on-success on-error]
   (-> (.fetch js/window url (clj->js {:method  "GET"
                                       :headers {"Cache-Control" "no-cache"}}))
       (.then (fn [response]
                (let [ok?  (.-ok response)
                      ok?' (if valid-response?
                             (and ok? (valid-response? response))
                             ok?)]
                  [(.-_bodyText response) ok?'])))
       (.then (fn [[response ok?]]
                (cond
                  ok? (let [json (.parse js/JSON response)
                            obj (js->clj json :keywordize-keys true)]
                        (on-success obj))

                  (and on-error (not ok?))
                  (on-error response)

                  :else false)))
       (.catch (or on-error
                   (fn [error]
                     (js/alert "Error" (str error))))))))
