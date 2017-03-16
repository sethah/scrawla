package scrape

import java.net.URL

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Utils {

  def getHTML(url: URL, timeout: Int = 3000): Document = {
    Jsoup.connect(url.toString).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .timeout(timeout)
      .get()
  }

}
