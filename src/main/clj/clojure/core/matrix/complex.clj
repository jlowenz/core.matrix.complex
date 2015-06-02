(ns clojure.core.matrix.complex
  (:require [clojure.core.matrix.protocols :as mp]
            [clojure.core.matrix :as m]
            [complex.core :as c]
            [clojure.core.matrix.implementations :as imp]
            [mikera.cljutils.error :refer :all])
  (:import [org.apache.commons.math3.complex Complex]))

(declare real imag complex-array)

;; The complex array class
;;
;; Implemented as a wrapper of two numerical arrays representing the real and imaginary parts

(deftype ComplexArray [real imag]
  mp/PImplementation 
	  (implementation-key [m]
	    :complex-array)
	  (meta-info [m]
	      {:doc "Implementation for complex arrays"})
	  (new-vector [m length] (ComplexArray. (m/new-vector length) (m/new-vector length)))
	  (new-matrix [m rows columns] 
      (ComplexArray. (m/new-matrix rows columns) (m/new-matrix rows columns)))
	  (new-matrix-nd [m dims]
	    (ComplexArray. (m/new-array dims) (m/new-array dims)))
	  (construct-matrix [m data]
	    (ComplexArray. (clojure.core.matrix.complex/real data) 
                     (clojure.core.matrix.complex/imag data)))
	  (supports-dimensionality? [m dims]
	    true)
   
   mp/PDimensionInfo
    (dimensionality [m]
      (mp/dimensionality (.real m)))
    (is-vector? [m]
      (mp/is-vector? (.real m)))
    (is-scalar? [m]
      false)
    (get-shape [m]
      (or (mp/get-shape (.real m)) []))
    (dimension-count [m x]
      (mp/dimension-count (.real m) x))
    
   mp/PIndexedAccess
    (get-1d [m x]
      (c/complex-number (double (mp/get-1d (.real m) x))
                        (double (mp/get-1d (.imag m) x))))
    (get-2d [m x y]
      (c/complex-number (double (mp/get-2d (.real m) x y))
                        (double (mp/get-2d (.imag m) x y))))
    (get-nd [m indexes]
      (c/complex-number (double (mp/get-nd (.real m) indexes))
                        (double (mp/get-nd (.imag m) indexes))))
    
    mp/PIndexedSetting
	    (set-1d [m row v]
	      (ComplexArray. (mp/set-1d real row (clojure.core.matrix.complex/real v))
                       (mp/set-1d imag row (clojure.core.matrix.complex/imag v))))
	    (set-2d [m row column v]
	      (ComplexArray. (mp/set-2d real row column (clojure.core.matrix.complex/real v))
                       (mp/set-2d imag row column (clojure.core.matrix.complex/imag v))))
	    (set-nd [m indexes v]
	      (ComplexArray. (mp/set-nd real indexes (clojure.core.matrix.complex/real v))
                       (mp/set-nd imag indexes (clojure.core.matrix.complex/imag v))))
	    (is-mutable? [m]
	      (mp/is-mutable? real))
     
     mp/PIndexedSettingMutable
     (set-1d! [m x v]
      (mp/set-1d! real x (clojure.core.matrix.complex/real v))
       (mp/set-1d! imag x (clojure.core.matrix.complex/imag v)))
     (set-2d! [m x y v]
      (mp/set-2d! real x y (clojure.core.matrix.complex/real v))
      (mp/set-2d! imag x y (clojure.core.matrix.complex/imag v)))
     (set-nd! [m indexes v]
      (mp/set-nd! real indexes (clojure.core.matrix.complex/real v))
      (mp/set-nd! imag indexes (clojure.core.matrix.complex/imag v)))
    
    java.lang.Object 
    (toString [m]
      (str "(" (str (.real m)) ", " (str (.imag m)) ")]"))
  
   clojure.lang.Seqable
     (seq [m]
       (map complex-array (m/slices (.real m)) (m/slices (.imag m))))

   mp/PMatrixAdd
     (matrix-add [m a]
       (ComplexArray. (mp/matrix-add (.real m) (clojure.core.matrix.complex/real a))
                      (mp/matrix-add (.imag m) (clojure.core.matrix.complex/imag a))))

   mp/PMatrixProducts
     (inner-product [m a]
       (ComplexArray. (mp/add-scaled (mp/inner-product (clojure.core.matric.complex/real m)
                                                       (clojure.core.matrix.complex/real a))
                                     (mp/inner-product (clojure.core.matric.complex/imag m)
                                                       (clojure.core.matrix.complex/imag a))
                                     -1)
                      (mp/matrix-add (mp/inner-product (clojure.core.matric.complex/real m)
                                                       (clojure.core.matrix.complex/imag a))
                                     (mp/inner-product (clojure.core.matric.complex/imag m)
                                                       (clojure.core.matrix.complex/real a))))))


(defn complex-array 
  ([real]
    (complex-array real 0.0))
  ([real imag]
    (ComplexArray. real (m/broadcast-coerce real imag))))

(defn real 
  "Get the real part of a complex array"
  ([m]
    (cond 
      (instance? ComplexArray m) (.real ^ComplexArray m)
      (instance? Complex m) (.getReal ^Complex m)
      (number? m) m
      (and (m/array? m) (m/numerical? m)) m
      :else (error "Unable to get real part of object: " m))))

(defn imag 
  "Get the imaginary part of a complex array"
  ([m]
    (cond 
	    (instance? ComplexArray m) (.imag ^ComplexArray m)
	    (instance? Complex m) (.getImaginary ^Complex m)
	    (number? m) 0.0
	    (and (m/array? m) (m/numerical? m)) (mp/coerce-param m (mp/broadcast-like m 0.0)) 
	    :else (error "Unable to get imaginary part of object: " m))))


(imp/register-implementation (complex-array 0 1))
