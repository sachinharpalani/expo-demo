(ns expo-demo.handlers
  (:require
    [re-frame.core :refer [dispatch reg-event-db ->interceptor reg-event-fx reg-fx]]
    [clojure.spec.alpha :as s]
    [expo-demo.db :as db :refer [app-db]]
    [expo-demo.api :refer [http-post http-get]]))

;; -- Interceptors ----------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/develop/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (->interceptor
        :id :validate-spec
        :after (fn [context]
                 (let [db (-> context :effects :db)]
                   (check-and-throw ::db/app-db db)
                   context)))
    ->interceptor))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
  :initialize-db
  [validate-spec]
  (fn [_ _]
    app-db))

(reg-event-db
 :set-greeting
 [validate-spec]
 (fn [db [_ value]]
   (assoc db :greeting value)))

(reg-event-db
 :set-page
 (fn [db [_ page]]
   (assoc db :page page)))

(reg-event-db
 :set-link
 (fn [db [_ link]]
   (assoc db :link link)))


(reg-event-fx
 :make-payment
 (fn [cofx [_ map]]
   {:process-payment map}))

(reg-event-fx
 :valid-pay
 (fn [cofx [_ response]]
   {:valid-pay response}))

(reg-event-fx
 :invalid-pay
 (fn [cofx [_ response]]
   (js/alert response)))


;;-- Effects -------------------------------------------------------------------------
(reg-fx
 :process-payment
 (fn [{:keys [amount purpose name]}]
   (http-post "https://test.instamojo.com/api/1.1/payment-requests/"
              {:amount amount
               :purpose purpose
               :buyer_name name}
              (fn [response] (dispatch [:valid-pay response]))
              {
               :X-Api-Key "b18d3f9192dab5b0da9b394e1f94b01a"
               :X-Auth-Token "34ed1dc866d0f6a3ece35b535e3439f9"}
              #(js/alert %))))


(def expo (js/require "expo"))
(def WB (.-WebBrowser expo))


(reg-fx
 :valid-pay
 (fn [response]
   (let [longurl (:longurl (:payment_request response))
         id (:id (:payment_request response))]
     (-> (.openBrowserAsync WB longurl)
         (.then (fn [r] (println id)))
         (.catch (fn [r] (println r))))
     #_(dispatch [:set-link longurl])
     #_(dispatch [:set-page :pay]))))
