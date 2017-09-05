(ns expo-demo.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [expo-demo.handlers]
              [expo-demo.subs]
              [cljs-exponent.contacts :as contacts]
              [cljs-exponent.permissions :as perm]
              [cljs-exponent.google :as google]
              [expo-demo.api :refer [http-post http-get]]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Alert (.-Alert ReactNative))

(defn alert [title]
  (.alert Alert title))

;;=====================COMMUNICATIONS==========================
(def COMM (js/require "react-native-communications"))


(defn get-contacts []
  (->(perm/ask-async "Permission.CONTACTS")
     (.then (fn [res] (js/alert res)))
     (.catch (fn [e] (js/alert e)))))

(def expo (js/require "expo"))
(def WB (.-WebBrowser expo))

(defn login []
  (-> (google/login-async (.stringify js/JSON (clj->js {:behavior "web"  :androidClientID "972048600409-n04ct3olhvtnjlt4osc3qn67pt1p3tal.apps.googleusercontent.com"
                                                        :scopes ["profile" "email"]})))
      (.then (fn [res] (js/alert res)))
      (.catch (fn [err] (js/alert err)))))


(defn pay-page []
  (.openBrowserAsync WB @(subscribe [:get-link])))



(defn home-page []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex 1 :flex-direction "column" :margin 40 :align-items "center" :justify-content "space-around"}}
       [image {:source (js/require "./assets/images/cljs.png")
               :style {:width 200
                       :height 200}}]

       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(dispatch [:set-page :phone])}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Phone-Demo"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(http-post "https://api.clevertap.com/1/upload"
                                                   {:d [{:identity "1234"
                                                         :type "event"
                                                         :evtName "charged"
                                                         :ectData {:Amount "3000"}}]}
                                                   (fn [r] (js/alert r))
                                                   {:X-CleverTap-Account-Id "466-77Z-4W5Z"
                                                    :X-CleverTap-Passcode "AMC-BMD-ASKL"})}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Add event"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(dispatch [:make-payment {:amount "500" :purpose "TEST-APP-PAYMENT" :name "SACHIN-TEST"}])}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Test g-login"]]])))

(defn phone-page []
  [view {:style {:flex 1  :justify-content "space-around"}}
   [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                         :on-press #(.phonecall COMM "+91 123456789" true)}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "CALL ME"]]
   [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                         :on-press #(.text COMM "1234567890" "Message")}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "TEXT ME"]]
   [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                         :on-press #(.email COMM (clj->js ["feedback@mindseed.in"]) nil nil "Hello" "TEST-EMAIL")}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "MAIL ME"]]
   [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                         :on-press #(dispatch [:set-page :home])}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "BACK"]]])

(def pages
  {:home #'home-page
   :phone #'phone-page
   :pay #'pay-page})

(defn app-root []
  [(pages @(subscribe [:get-page]))])

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))
