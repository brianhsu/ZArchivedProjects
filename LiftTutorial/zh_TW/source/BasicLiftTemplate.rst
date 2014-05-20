Lift 模版系統基礎
#####################

Lift 的模版系統與其他常見的 Web Framework 相當不同，在 Lift 裡面，我們不是在模版裡執行特定的程式碼來組合出最後的結果。相反地，在 Lift 裡面，模版是單純的 xHTML / HTML5 的檔案，當我們需要注入動態內容的時候，是將 xHTML / HTML5 當中的節點，做為參數傳遞給 Scala 函式做處理，並且以處理過後的節點取代新的節點。

這是在 Function Programming 常見的一種做法，我們將資料經過特定的函式進行轉換後取得新的資料。

然而由於這種做法在其他的網頁應用程式框架與模版系統中並不常見到，因此初次接觸 Lift 的使用者，往往不知道該如何達成某些在其他框架的模版系統中相當直覺的功能－－例如，如果模版中不能有迴圈的話，那我們怎麼顯示具有重覆性的資料，像是表格或是清單？

因此我們在這一章會介紹 Lift 的模版系統基礎，告訴你如何利用 Lift 的模版系統，組合出你想要的 HTML 結果。


Functional Programming 一些常見的特性
=========================================

如果你已經接觸了 Scala 一段時間，那應該聽過 `Functional Programming`_ 這種編程典範，以及其中的一些特性，以及使用過其中的某些功能，例如 ``List.map()`` 此函式。

在 Lift 的模版系統中也大量運用到 Functional Programming 的概念，因此在這一節中我們會復習一些 Functional Programming 中常見的特性。

Immutable Data Sturcture
-------------------------

在 Function Programming 的典範中，資料結構（物件）被建立後就是不可以改變的，任何對於該物件的操作（例如呼叫函式）若是會產生狀態的改變，那麼實際上是產生一個新的物件。

Java / Scala 當中的字串也有同樣的特性，以下面的程式碼為例，你可以看到當我們針對 ``str1`` 呼叫 ``substring(0,5)`` 取回前五個字元時，事實上是產生了一個新的字串物件，原先 ``str1`` 所指到的 ``Hello World`` 字串仍然是維持原樣，並未被改變。

.. code-block:: scala

    scala> val str1 = "Hello World"
    str1: String = Hello World

    scala> val str2 = str1.substring(0, 5)
    str2: String = Hello

    scala> str1
    res0: String = Hello World

同樣的，我們在 Scala 中經常使用到的 ``List`` 物件，也是 Immutable Data Structure，當我們呼叫例如 ``take`` 等函式時，Scala 事實上是產生了一個新的 ``List`` 做為回傳值，而原本的物件並沒有被改變。

.. code-block:: scala

    scala> val xs = List(1, 3, 2, 4, 5, 6, 9)
    xs: List[Int] = List(1, 3, 2, 4, 5, 6, 9)

    scala> val ys = xs.take(3)
    ys: List[Int] = List(1, 3, 2)

    scala> xs
    res1: List[Int] = List(1, 3, 2, 4, 5, 6, 9)



High-order Function
---------------------

在 Functional Programming 中，函式是 `First-class citizen`_\ ，這代表了函式與整數或物件等東西一樣，可以做為函式的參數，亦或是回傳值。

例如我們在 Scala 中經常使用的 ``List.map()`` 函式，他所接受的參數就是另一個函式，假使我們要將某個 List 中的整數都加二的話，那只要傳遞一個接受一個整數，並返回一個整數的函式即可。

.. code-block:: scala

    scala> val plusOne = (x: Int) => x + 1
    plusOne: Int => Int = <function1>

    scala> List(1, 3, 4, 10).map(plusOne)
    res2: List[Int] = List(2, 4, 5, 11)

另一方面，函式也可以做為回傳值，例如我們可以寫一個函式，其回傳值是另一個函式：

.. code-block:: scala

    scala> def createAFunc(add: Int) = (x: Int) => x + add
    createAFunc: (add: Int)Int => Int

    scala> val plusSeven = createAFunc(7)
    plusSeven: Int => Int = <function1>

    scala> val plusFive = createAFunc(5)
    plusFive: Int => Int = <function1>

    scala> plusSeven(1)
    res3: Int = 8

    scala> plusFive(1)
    res4: Int = 6

在上面的程式碼範例中，我們撰寫了一個 ``createAFunc`` 函式，其回傳的是另一個接受一個整數參數 x 並加上 add 的函式，接著我們再透過呼叫 ``createAFunc(7)`` 和 ``createAFunc(5)`` 來建立兩個新的函式，並且使用這兩個函式。

Composable Function
---------------------

另一個 Functional Programming 常見的特性是我們可以透過組合不同的函式來得到新的函式，在 Scala 中我們可以用 ``andThen`` 與 ``compose`` 來達成這件事情。

舉例來說，我們可以用 ``andThen`` 來以左結合的方式來組合兩個函式，當我們呼叫組合出的新函式時，會先呼叫左邊的函式，並且將左邊函式的結果傳入給右邊的函式，最後得到結果。

.. code-block:: scala

    scala> def plusOne(x: Int) = x + 1
    plusOne: (x: Int)Int
    
    scala> def timesFive(x: Int) = x * 5
    timesFive: (x: Int)Int
    
    scala> val newFunc = plusOne _ andThen timesFive _
    newFunc: Int => Int = <function1>
    
    scala> newFunc(2)   // 等同 timesFive(plusOne(2))
    res6: Int = 15

我們也可以使用 ``compose`` 以右結合的方式來組合函式，如下所示，若我們將 ``andThen`` 代換成 ``compose`` 的話，會先執行 ``timesFive`` 再執行 ``plusOne`` 這個函式。

.. code-block:: scala

    scala> def plusOne(x: Int) = x + 1
    plusOne: (x: Int)Int
    
    scala> def timesFive(x: Int) = x * 5
    timesFive: (x: Int)Int
    
    scala> val newFunc = plusOne _ compose timesFive _
    newFunc: Int => Int = <function1>

    scala> newFunc(2)  // 等同 plusOne(timesFive(2))
    res0: Int = 11

Lift 模版系統基本流程
=======================

上一節我們看到了 Functional Programming 的一些特性，在這一節當中，我們會介紹 Lift 的 Template / Snippet 機制的基本流程，並且在下一節中介紹這些機制與 Functional Programming 的一些特性的對應。

在模版中指定要呼叫的函式
-------------------------------------

由於在 Lift 的模版系統裡不能有任何的程式碼，因此在 Lift 中我們是把模版中的 HTML 節點做為參數傳入特定的 Function 中，並且將該 Function 回傳的新 HTML 節代取原先的節點。

在使用上，當 Lift 的模版系統看到模版中的 HTML 節點有 ``data-lift="ClassName.method"`` 屬性時，會把該節點做為參數傳入給 ``ClassName`` 物件的 ``method`` 函式，我們稱這種函式為 Snippet。

舉例而言，以下的 HTML 模版會將 ``<span>`` 節點傳入 ``MySnippet`` 物件裡的 ``currentDate`` 函式並且以回傳值取代原先的節點；同樣的，其中的 ``<div>`` 節點會被傳入 ``MySnippet`` 物件的 ``currentTime`` 函式，並以回傳值取代原先的節點，而其他的節點則維持原樣。

.. code-block:: html

   <html>
     <body>
       <h1>Hello World</h1>
       <span data-lift="MySnippet.currentDate">2013-03-03</span>
       <div data-lift="MySnippet.currentTime">13:22</span>
     </body>
   </html>

除了像上述方式指定完整的類別與函式名稱外，Lift 也支援了以下幾種型式：

  ``data-lift="MySnippet"``
    如果在 ``data-lift`` 中只有類別名稱而沒有函式名稱，Lift 會使用 ``render`` 這個函式。
  
  ``data-lift="my_snippet.current_time"``
    除了使用 CamelCase 之外，你也可以使用底線的命名風格，但 Lift 在尋找要使用的函式時會將其轉換成 CamelCase 格式，所以在這個例子中，Lift 仍然會去尋找 ``MySnippet.currentTime`` 此函式。

  ``data-lift="my_snippet"``
    使用底線做為分隔符號的時候，一樣可以省略函式的名稱，讓 Lift 使用預設的 ``render`` 這個函式。

此外值得注意的是，我們在 HTML 模版當中指定的只有類別名稱，而 Lift 會到我們在 ``Boot.scala`` 中使用 ``LiftRules.addToPackages`` 當中指定的 Package 中的 snippet 子 package 中尋找該類別。

舉例而言，若我們的 Boot.scala 裡有下列的設定：

.. code-block:: scala

    LiftRules.addToPackages("net.myproject")

那麼在上面的例子中，Lift 會使用 ``net.myproject.snippet.MySnippet`` 這個類別。

基本流程－－撰寫 Snippet 函式
-------------------------------------

當我們在 HTML 模版中指定了要使用的 Snippet 之後，我們需要實作模版中指定的 Snippet 類別與函式。

在 Lift 當中，基本的 Snippet 函式會有以下兩個兩個種類：

- ``def render(html: NodeSeq): NodeSeq``
- ``def render: CssSel``

在第一個類型中，我們的 Snippet 函式接收一個代表 HTML 節點的 `scala.xml.NodeSeq`_ 物件，並自己處理該節點後，返回一個新的 ``NodeSeq`` 物件，Lift 會將原先的節點用這個新的 ``NodeSeq`` 物件取代。

當實作 ``def render(html: NodeSeq): NodeSeq`` 這種型式的 Snippet 時，我們可以直接使用 `scala.xml`_ 這個 Package 所提供的 API，亦或是透過 `net.liftweb.util.BindHelpers`_ 來對節點進行處理。

第二種型式的 Snippet 函式，我們返回的是一個 `net.liftweb.util.CssSel`_ 物件，而這個物件本身就是一個 ``NodeSeq => NodeSeq`` 的函式，所以 Lift 一樣可以將使用者標注的節點傳入給 ``render`` 回傳的 ``CssSel`` 物件做處理，並且取代原先的節點。

關於 ``CssSel`` 的使用，我們會在之後的章節中詳細介紹。

xHTML vs HTML5
------------------

Lift 預設模版和輸出網頁均使用 xHTML 格式，但也支援以下兩種設定：

- 模版和輸出網頁均為 HTML5
- 模版為 xHTML，輸出網頁為 HTML5

我們可以使用在 ``Boot.scala`` 使用 ``LiftRules.htmlProperties`` (`ScalaDoc <http://liftweb.net/api/25-rc1/api/#net.liftweb.http.LiftRules>`_) 來進行設定，若要使用 HTML5 做為模版，可以使用以下的設定，使用 `Html5Properties`_ 物件來指定我們的模版與輸出都是 HTML5：

.. code-block:: scala

    import net.liftweb.http.Req
    import net.liftweb.http.Html5Properties
    import net.liftweb.http.LiftRules

    class Boot {
      def boot {
        LiftRules.htmlProperties.default.set { r: Req => new Html5Properties(r.userAgent) }
      }
    }

若要使用 xHTML 做為網頁模版，但在輸出的時候使用 HTML5 的話，可以在 ``Boot.scala`` 中使用下列的設定，並傳入 `XHtmlInHtml5OutProperties`_ 物件做設定：

.. code-block:: scala

    import net.liftweb.http.Req
    import net.liftweb.http.XHtmlInHtml5OutProperties
    import net.liftweb.http.LiftRules

    class Boot {
      def boot {
        LiftRules.htmlProperties.default.set { r: Req => new XHtmlInHtml5OutProperties(r.userAgent) }
      }
    }

當我們使用以上的設定的時候，Lift 會以 xHTML 的 Parser 來解析模版，但是在輸出成網頁的時候，將其轉換成為 HTML5 的格式。

CSS Selector Binding
=======================

上一節當中，我們看到了在 Lift 裡實作 Snippet 有兩種方式，第一種是透過操作傳入的 ``NodeSeq`` 節點，來直接回傳一個新的 ``NodeSeq`` 節點，讓 Lift 取代原有的 HTML 節點。

但是多數的時候，直接操作 HTML 節點並不是那麼直覺，因此 Lift 提供了 CSS Selector Transforms，讓使用者使用 `CSS Selector`_ 的方式來指定要如何處理模版中的 HTML 節點。

當使用 CSS Selector 實作 Snippet 的時候，我們的 Snippet 函式的宣告格式如下：

.. code-block:: scala

    def render: CssSel

我們可以看到，與原先的 ``def render(xhtml: NodeSeq): NodeSeq`` 不同，在這裡我們的 ``redner`` 函式沒有傳入值，而回傳值是一個 `net.liftweb.util.CssSel`_ 物件。

不過由於 ``CssSel`` 本身就是一個 ``NodeSeq => NodeSeq`` 的函式，也就是說 ``CssSel`` 可以接受一個 ``NodeSeq`` 物件並返回另一個 ``NodeSeq`` 物件，因此我們的 ``render`` 實際上是返回了另一個函式供 Lift 模版系統呼叫，將被標註的 HTML 節點傳入 ``CssSel`` 函式做處理，並以其回傳值取代原先的 HTML 節點。


以 Functional Programming 的角度來看 Snippet
================================================

Immutable Data Structure
----------------------------

High-order Function
----------------------------

Compsable Function
----------------------------

內建的 Snippet 介紹
====================

.. _Functional Programming: http://en.wikipedia.org/wiki/Functional_programming
.. _First-class Citizen: http://en.wikipedia.org/wiki/First-class_citizen
.. _Html5Properties: http://liftweb.net/api/25-rc1/api/#net.liftweb.http.Html5Properties
.. _scala.xml.NodeSeq: http://www.scala-lang.org/api/current/index.html#scala.xml.NodeSeq
.. _net.liftweb.util.CssSel: http://liftweb.net/api/25-rc1/api/#net.liftweb.util.CssSel
.. _XHtmlInHtml5OutProperties: http://liftweb.net/api/25-rc1/api/#net.liftweb.http.XHtmlInHtml5OutProperties
.. _scala.xml: http://www.scala-lang.org/api/current/scala/xml/package.html
.. _net.liftweb.util.BindHelpers: http://liftweb.net/api/25-rc1/api/#net.liftweb.util.BindHelpers$
.. _CSS Selector: http://www.w3.org/TR/CSS2/selector.html
