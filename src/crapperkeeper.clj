(ns crapperkeeper
  (:require [crapperkeeper.internal :as internal]
            [crapperkeeper.schemas :refer :all]
            [schema.core :as schema])
  (:import (clojure.lang Keyword)
           (java.util Map)))

(schema/defn ^:always-validate service-call
  "Inovkes the function named by 'fn-key' on the service-interface
  specified by 'service-interface' using the given arguments."
  [state :- Map
   service-interface :- ServiceInterface
   fn-key :- Keyword
   & args]
  (if-let [service (first (filter
                            #(= (:implements %) service-interface)
                            @(:services state)))]
    (let [service-fn (get-in service [:service-fns fn-key])
          context (get @(:contexts state) (:id service))]
      (apply service-fn state context args))
    ; TODO should this behave differently if the service is an optional vs. completely non-existent?
    ; TODO I think so ... I think the latter should just be an error.
    ; TODO see with-optional-dependencies
    ; TODO definitely need a LOT more error handling in here, it's easy to get wrong
    #_(log/info "service-call doing nothing because no implementation of"
              service-interface "available")))

(schema/defn ^:always-validate shutdown!
  "Stops the Trapperkeeper framework and all services running within it.
  Calls 'stop' on each service."
  [state :- Map]
  (internal/run-lifecycle-fns
   state
   :stop
   @(:services state)
   @(:contexts state))
  ;; TODO (deliver shutdown-promise)
  )
