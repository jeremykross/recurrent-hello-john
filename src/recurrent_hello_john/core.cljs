(ns recurrent-hello-john.core
  (:require 
    garden.core
    recurrent.drivers.dom
    [dommy.core :include-macros true]
    [recurrent.core :as recurrent :include-macros true]
    [ulmus.core :as ulmus]))


(recurrent/defcomponent TextInput

  ; keys expected in props and sources
  [:initial-value] [:dom-$]

  ; constructor, return value used as the value of this
  #()

  ; some css, garden style
  [:.text-input {:margin "16px"}]

  ; Our first return signal (called a sink)
  ; creates a signal of successive virutal-dom (hiccup style) to get diffed into real dom.
  :dom-$ (fn [this props sources sinks]
           (ulmus/map (fn [value]
                        [:input {:class "text-input"
                                 :type "text"
                                 :value value}])
                      (:value-$ sinks)))

  ; Another sink
  ; Keeps track of the current value
  :value-$ (fn [this props sources sinks]
             (ulmus/start-with! (:initial-value props)
               (ulmus/map
                 (fn [evt] (.-value (.-target evt)))
                 ; a special souce, available under the first key in the source list.
                 ; takes a query-selector and an event
                 ; returns a signal of occurances of the event.
                 ((:dom-$ sources) "input" "keydown")))))

(recurrent/defcomponent Label
  [] [:dom-$ :name-$] 

  ; Constructor
  #()

  ; Style
  [:label {:margin-left "42px"}]

  :dom-$ (fn [_ _ sources _]
           (ulmus/map
             (fn [n]
               [:label (str "Hello, " n ".")])
             (:name-$ sources))))


(defn Main
  [props sources]
  (let [input (TextInput {:initial-value "John"} {:dom-$ (:dom-$ sources)})
        label (Label {} {:dom-$ (:dom-$ sources)
                         :name-$ (:value-$ input)})]
    {:dom-$
     (ulmus/map
       (fn [[input-dom label-dom]]
         [:div input-dom label-dom])
       (ulmus/latest (:dom-$ input) (:dom-$ label)))}))
      
(defn main!
  []
  (recurrent/run!
    Main
    {:dom-$ (recurrent.drivers.dom/from-id "app")}))
