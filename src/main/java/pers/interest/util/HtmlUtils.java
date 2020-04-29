package pers.interest.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HtmlUtils {
  /**
   * 正则匹配loginhash
   */
  private final static Pattern loginhashPattern = Pattern.compile("(?<=loginhash=)([^\\\"]*)");
  /**
   * 正则匹配图片路径
   */
  private final static Pattern pacturePathPattern = Pattern.compile("(?<=zoomfile=\\\")([^\\\"]*)");

  /**
   * 解析html获取loginhash值，这个是在url中所以直接通过正则提取
   */
  public static String getLoginhash(String html) {
    Matcher loginhashMtcher = loginhashPattern.matcher(html);
    String loginhash = null;
    while (loginhashMtcher.find()) {
      loginhash = loginhashMtcher.group(1);
      break;
    }
    return loginhash;
  }

  /**
   * 解析html获取formhash值
   */
  public static String getFormhash(String html) {
    Document document = Jsoup.parse(html);
    Element element = document.body();
    String formhash = element.selectFirst("input[name=formhash]").attr("value");
    return formhash;
  }

  /**
   * 获取html的title并去除论坛后缀
   *
   * @param html
   * @return
   */
  public static String getTitle(String html) {
    Document document = Jsoup.parse(html);
    return document.title().trim();
  }

  /***
   * 获取并拼接图片url
   * 注意这只是通用的请求，绝大部分是可以的，有些帖子漫画得点击按钮才会出现，像这种就不行
   */
  public static List<String> getImageUrls(String html, String serverUrl) {
    Document document = Jsoup.parse(html);
    List<String> images = document.select("img[class=zoom]").eachAttr("file");
    images = images.stream().map(suffix -> {
      // emmm...坑啊，有的页面file属性包含域名信息
      if (suffix.contains(serverUrl)) {
        return suffix;
      }
      StringBuilder url = new StringBuilder(serverUrl).append("/").append(suffix);
      return url.toString();
    }).collect(Collectors.toList());
    return images;
  }

  /**
   * 根据收藏页获取所有的收藏页面
   * @param html
   * @param favoritePageUrl
   * @return
   */
  public static List<String> getFavoritePages(String html, String favoritePageUrl) {
    List<String> urls = new ArrayList<>();
    Document document = Jsoup.parse(html);
    Element custompage = document.selectFirst("input[name=custompage]");
    if (null != custompage) {
      int pages = Integer.valueOf(custompage.attr("size"));
      for (int i = 1; i <= pages; i++) {
        StringBuilder url = new StringBuilder(favoritePageUrl).append("&page=").append(i);
        urls.add(url.toString());
      }
    }
    return urls;
  }

  /**
   * 获取帖子收藏页的全部url
   */
  public static List<String> getFavoriteUrls(String html, String serverUrl) {
    Document document = Jsoup.parse(html);
    Elements select = document.select("ul[id=favorite_ul]").select("a[target=_blank]");
    List<String> urls = select.eachAttr("href").stream().map(suffix -> {
      StringBuilder url = new StringBuilder(serverUrl).append("/").append(suffix);
      return url.toString();
    }).collect(Collectors.toList());
    return urls;
  }

  /**
   * 获取只看楼主的url
   *
   * @param html
   * @return
   */
  public static String getOnlyLandlordUrl(String html, String serverUrl) {
    Document document = Jsoup.parse(html);
    String suffix = document.selectFirst("a[rel=nofollow]").attr("href");
    StringBuilder url = new StringBuilder(serverUrl).append("/").append(suffix);
    return url.toString();
  }

  public static void main(String[] args) {

    Document doc = Jsoup.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
      "<html>\n" +
      "<head>\n" +
      "\t<title>bbs.yamibo.com - System Error</title>\n" +
      "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gbk\" />\n" +
      "\t<meta name=\"ROBOTS\" content=\"NOINDEX,NOFOLLOW,NOARCHIVE\" />\n" +
      "\t<style type=\"text/css\">\n" +
      "\t<!--\n" +
      "\tbody { background-color: white; color: black; font: 9pt/11pt verdana, arial, sans-serif;}\n" +
      "\t#container { width: 1024px; }\n" +
      "\t#message   { width: 1024px; color: black; }\n" +
      "\n" +
      "\t.red  {color: red;}\n" +
      "\ta:link     { font: 9pt/11pt verdana, arial, sans-serif; color: red; }\n" +
      "\ta:visited  { font: 9pt/11pt verdana, arial, sans-serif; color: #4e4e4e; }\n" +
      "\th1 { color: #FF0000; font: 18pt \"Verdana\"; margin-bottom: 0.5em;}\n" +
      "\t.bg1{ background-color: #FFFFCC;}\n" +
      "\t.bg2{ background-color: #EEEEEE;}\n" +
      "\t.table {background: #AAAAAA; font: 11pt Menlo,Consolas,\"Lucida Console\"}\n" +
      "\t.info {\n" +
      "\t    background: none repeat scroll 0 0 #F3F3F3;\n" +
      "\t    border: 0px solid #aaaaaa;\n" +
      "\t    border-radius: 10px 10px 10px 10px;\n" +
      "\t    color: #000000;\n" +
      "\t    font-size: 11pt;\n" +
      "\t    line-height: 160%;\n" +
      "\t    margin-bottom: 1em;\n" +
      "\t    padding: 1em;\n" +
      "\t}\n" +
      "\n" +
      "\t.help {\n" +
      "\t    background: #F3F3F3;\n" +
      "\t    border-radius: 10px 10px 10px 10px;\n" +
      "\t    font: 12px verdana, arial, sans-serif;\n" +
      "\t    text-align: center;\n" +
      "\t    line-height: 160%;\n" +
      "\t    padding: 1em;\n" +
      "\t}\n" +
      "\n" +
      "\t.sql {\n" +
      "\t    background: none repeat scroll 0 0 #FFFFCC;\n" +
      "\t    border: 1px solid #aaaaaa;\n" +
      "\t    color: #000000;\n" +
      "\t    font: arial, sans-serif;\n" +
      "\t    font-size: 9pt;\n" +
      "\t    line-height: 160%;\n" +
      "\t    margin-top: 1em;\n" +
      "\t    padding: 4px;\n" +
      "\t}\n" +
      "\t-->\n" +
      "\t</style>\n" +
      "</head>\n" +
      "<body>\n" +
      "<div id=\"container\">\n" +
      "<h1>Discuz! System Error</h1>\n" +
      "<div class='info'><li>您当前的访问请求当中含有非法字符，已经被系统拒绝</li></div>\n" +
      "\n" +
      "<div class=\"info\"><p><strong>PHP Debug</strong></p><table cellpadding=\"5\" cellspacing=\"1\" width=\"100%\" class=\"table\"><tr><td><ul><li>[Line: 0026]member.php(discuz_application->init)</li><li>[Line: 0071]source/class/discuz/discuz_application.php(discuz_application->_init_misc)</li><li>[Line: 0558]source/class/discuz/discuz_application.php(discuz_application->_xss_check)</li><li>[Line: 0359]source/class/discuz/discuz_application.php(system_error)</li><li>[Line: 0023]source/function/function_core.php(discuz_error::system_error)</li><li>[Line: 0024]source/class/discuz/discuz_error.php(discuz_error::debug_backtrace)</li></ul></td></tr></table></div><div class=\"help\"><a href=\"http://bbs.yamibo.com\">bbs.yamibo.com</a> 已经将此出错信息详细记录, 由此给您带来的访问不便我们深感歉意. </div>\n" +
      "</div>\n" +
      "</body>\n" +
      "</html>");
//      Document doc = Jsoup.connect("https://bbs.yamibo.com/member.php?mod=logging&action=login").get();
    Element element = doc.selectFirst("input[name=custompage]");
//      String formhash = element.selectFirst("input[name=formhash]").attr("value");

    System.out.println("formhash = " + element);

  }
}
