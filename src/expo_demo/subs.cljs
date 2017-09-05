(ns expo-demo.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-greeting
 (fn [db _]
   (:greeting db)))

(reg-sub
 :get-page
 (fn [db _]
   (:page db)))

(reg-sub
 :get-link
 (fn [db _]
   (or (:link db) "http://wwww.google.com")))
