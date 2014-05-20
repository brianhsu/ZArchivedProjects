package org.maidroid.utils

import scala.io._
import scala.xml.XML
import scala.xml.Node

import java.net.URL

case class GeoPlacemark (val query: String, val address: String, 
                         val accuracy: Int, val longitude: Double,
                         val latitude: Double)
{
    val longitudeE6 = (longitude * 1E6) toInt
    val latitudeE6  = (latitude  * 1E6) toInt
}

class GeoService (val query: String, val locale: String)
{
    lazy val placemarkList: List[GeoPlacemark] = doQuery ()

    private var statusCode   = 0
    private var errorMessage = ""

    def this (query: String) = this (query, "")
    def error = (statusCode, errorMessage)

    private def createGeoPlacemark (node: Node) = 
    {
        val address    = (node \ "address").text
        val accuracy   = (node \ "AddressDetails" \ "@Accuracy").text
        val coordinate = (node \ "Point").text.split (",").toList
        val List (longitude, latitude, _) = coordinate

        GeoPlacemark (query, address, accuracy.toInt, 
                      longitude.toDouble, latitude.toDouble)
    }

    private def loadXMLFromGoogleMaps () = {
        val url = "http://maps.google.com/maps/geo?q=%s&output=xml&gl=%s".
                  format(query, locale)

        val source = Source.fromURL (new URL(url))
        val iter   = for (line <- source.getLines()) yield line
        val xml    = XML.loadString (iter.mkString)

        val statusCode = (xml \ "Response" \ "Status" \ "code").text.toInt

        (xml, statusCode)
    }

    private def doQuery () =
    {
        try {
            val (xml, statusCode) = loadXMLFromGoogleMaps

            // code 200 means OK
            if (statusCode != 200) {
                this.statusCode = statusCode
                throw new Exception ("Query Faild")
            }

            val placemark   = xml \ "Response" \ "Placemark"
            val addressList = for (node <- placemark) yield 
                                  createGeoPlacemark (node)

            addressList.toList
        } catch {
            case e => errorMessage = e.getMessage
                      Nil
        }
    }
}


