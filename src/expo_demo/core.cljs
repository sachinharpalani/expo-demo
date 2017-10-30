(ns expo-demo.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [expo-demo.handlers]
              [expo-demo.subs]
              [cljs-exponent.contacts :as contacts]
              [cljs-exponent.permissions :as perm]
              [cljs-exponent.google :as google]
              [expo-demo.api :refer [http-post http-get]])
    (:require-macros [adzerk.env :as env]))

(env/def
  FOO nil)

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
(def goog (.-Google expo))
(def Video (.-Video expo))

(def vp (js/require "@expo/videoplayer"))
(def videoplayer (r/adapt-react-class (.-default vp)))


(defn login []
  (-> (.logInAsync goog (clj->js {:androidClientId "972048600409-n04ct3olhvtnjlt4osc3qn67pt1p3tal.apps.googleusercontent.com"}))
      (.then (fn [res] (println (js->clj res :keywordize-keys true))))
      (.catch (fn [e] (println e)))))

(defn pay-page []
  (.openBrowserAsync WB @(subscribe [:get-link])))

(defn video-page []
  [view {:flex 1}
   [videoplayer
    {:videoProps {:shouldPlay false
                  :source {:uri "http://d23dyxeqlo5psv.cloudfront.net/big_buck_bunny.mp4"}}
     :isPortrait true
     :playFromPositionMillis 0}]
   [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                         :on-press #(dispatch [:set-page :home])}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "BACK"]]])

(def intro (r/adapt-react-class (aget (js/require "react-native-app-intro") "default")))

(defn intro-page []
  [intro {:on-skip-btn-click #(js/alert "Skip")
          :on-done-btn-click #(js/alert "Done")
          :custom-styles {:btn-container {:flex 1 :align-items "center"}}}

   [view {:style {:flex 1 :justify-content "center" :align-items "center" :background-color "#9DD6EB" :padding 15}}
    [view {:level 10}
     [text {:style {:color "#fff" :font-size 30 :font-weight "bold"}} "Page 1"]]]
   [view {:style {:flex 1 :justify-content "center" :align-items "center" :background-color "#a4b602" :padding 15}}
    [view {:level 10}
     [text {:style {:color "#fff" :font-size 30 :font-weight "bold"}} "Page 2"]]]
   [view {:style {:flex 1 :justify-content "center" :align-items "center" :background-color "#fa931d" :padding 15}}
    [view {:level 10}
     [text {:style {:color "#fff" :font-size 30 :font-weight "bold"}} "Page 3"]]]])


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
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Pay Instamojo"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(login)}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "G-LOGIN"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(dispatch [:set-page :video])}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Video Demo"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(dispatch [:set-page :intro])}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Intro Demo"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(dispatch [:set-page :joy])}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "JoyRide Demo"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(js/alert (type FOO))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "GET ENV"]]])))

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

(def JoyRide (js/require "react-native-joyride"))
(def Joy (.joyride JoyRide))
(def JoyrideStep  (r/adapt-react-class (.-JoyrideStep JoyRide)))
(def JoyrideText  (r/adapt-react-class (.joyridable JoyRide (clj->js (.-Text ReactNative)))))

(defn joyride-page [props]
  (let [{:keys [start visible]} (js->clj props :keywordize-keys true)]
    [view
     [JoyrideStep {:text  "Hey! This is the first step of the tour!"
                   :order 1
                   :name "first"}
      [JoyrideText "\n\n\n\nWelcome to the demo of joyride library"]]
     [JoyrideStep {:text  "Hey! This is the SECOND step of the tour!"
                   :order 2
                   :name "second"}
      [JoyrideText "\n\n\n\n\nWelcome to the demo of joyride library"]]
     [JoyrideStep {:text  "Hey! This is the tHIRD step of the tour!"
                   :order 3
                   :name "third"}
      [JoyrideText "\n\n\n\n\nWelcome to the demo of joyride library"]]
     [touchable-highlight {:on-press #(start)}
      [text {:style {:text-align "center" :font-weight "bold"}} "START TUTORIAL"]]]))

(defn m-joyride-page []
  (let [m (-> joyride-page
              r/reactify-component
              Joy
              r/adapt-react-class)]
    [m]))

(def pages
  {:home #'home-page
   :phone #'phone-page
   :pay #'pay-page
   :video #'video-page
   :intro #'intro-page
   :joy #'m-joyride-page})

(defn app-root []
  [(pages @(subscribe [:get-page]))])
(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))
