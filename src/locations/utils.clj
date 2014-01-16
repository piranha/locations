(ns locations.utils)


(defmacro <? [promise]
  `(cljs.core.async/<!
    (locations.utils/<?* ~promise)))
