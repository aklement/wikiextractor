import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.xml.sax.SAXException;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.dump.WikiXMLParser;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.DefaultEventListener;
import info.bliki.wiki.model.WikiModel;

public class WikiExtractPairs
{
  
  protected static final String WIKI_URL_PREFIX = "http://";
  protected static final String WIKI_URL_SUFFIX = ".wikipedia.org/wiki/${title}";
  
  protected static final String OTHER_PREFIX = "=";
  
  protected static final String WIKIDUMP_SUFFIX = "wiki-latest-pages-articles.xml";
  protected static final String WIKIDUMP_ENGLISH_TEST = "/Users/aklement/Resources/WikiDumps/en/test.xml";
  
  public static void main(String[] args) throws SAXException, IOException
  {
    if (args != null && args.length == 4)
    {
      String dumpFile = args[2] + "/" + args[0] + WIKIDUMP_SUFFIX; 
            
      WikiXMLParser parser = new WikiXMLParser(new BufferedInputStream(new FileInputStream(dumpFile)), new ArticleHandler(args[0], args[1], args[3]));
      parser.parse();
      System.out.println("Done.\n");
    }
    else
    {
      System.out.println("Usage WikiExtractPairs orig_lang inter_lang in_Path out_Path");
    }
  }
  
  /**
   * Persists a parsed wiki article if it contains a link to a specified
   * language.
   */
  static class ArticleHandler extends DefaultEventListener implements IArticleFilter
  {
    protected static final String[] WIKI_LANGS = {"en", "de", "fr", "pl", "it", 
      "ja", "nl", "es", "pt", "ru", "sv", "zh", "no", "fi", "ca", "uk", "hu", 
      "cs", "tr", "ro", "ko", "eo", "da", "ar", "vo", "id", "sk", "vi", "sr", 
      "he", "lt", "bg", "fa", "sl", "hr", "et", "new", "ms", "simple", "th", 
      "gl", "nn", "hi", "ht", "eu", "el", "te", "ceb", "mk", "ka", "la", "br", 
      "az", "bs", "lb", "sh", "is", "mr", "cy", "sq", "lv", "bpy", "tl", "jv", 
      "pms", "be-x-old", "bn", "ta", "oc", "an", "io", "be", "sw", "nds", "scn", 
      "su", "fy", "af", "zh-yue", "nap", "ku", "ast", "ur", "bat-smg", "gu", "ml",
      "qu", "wa", "war", "cv", "ksh", "ga", "tg", "roa-tara", "vec", "kn", "gd",
      "uz", "pam", "lmo", "yi", "hy", "mi", "zh-min-nan", "nah", "glk", "hsb",
      "sah", "kk", "co", "als", "li", "roa-rup", "ia", "tt", "yo", "bcl", "os", 
      "gan", "arz", "fiu-vro", "nds-nl", "mn", "vls", "tk", "sa", "fo", "am",
      "nrm", "dv", "pag", "rm", "map-bms", "wuu", "gv", "bar", "ne", "my", "sco",
      "diq", "se", "fur", "pnb", "lij", "nov", "si", "mt", "bh", "mzn", "csb",
      "ilo", "pi", "zh-classical", "lad", "km", "ug", "sc", "frp", "mg", "ang",
      "kw", "pdc", "haw", "szl", "ps", "hif", "bo", "kv", "pa", "ie", "to", "hak",
      "myv", "crh", "gn", "ln", "stq", "nv", "jbo", "arc", "mhr", "ext", "wo",
      "ace", "ky", "tpi", "ty", "cbk-zam", "so", "eml", "ckb", "zea", "srn", "ay",
      "pap", "kab", "ig", "kg", "ba", "or", "lo", "udm", "dsb", "rmy", "cu",
      "kaa", "ce", "mo", "ab", "sm", "av", "xal", "ks", "tet", "got", "sd", "mdf",
      "na", "pnt", "iu", "kl", "bm", "pih", "as", "pcd", "cdo", "mwl", "chr",
      "ee", "fj", "om", "zu", "ti", "ts", "za", "ss", "ve", "bi", "ha", "dz",
      "bxr", "cr", "ch", "bug", "xh", "tn", "ki", "ik", "rw", "st", "ny", "tw",
      "chy", "ak", "sn", "ff", "lg", "lbe", "sg", "tum", "rn", "ng", "ii", "cho",
      "mh", "aa", "kj", "ho", "mus", "kr", "hz", "tokipona"};
    
    protected final String LANG_ENGLISH = "en";
    
    public ArticleHandler(String origLang, String interLang, String outPath)
    {
      m_config = Configuration.DEFAULT_CONFIGURATION;
      
      for (String lang : WIKI_LANGS)
      { m_config.addInterwikiLink(lang, WIKI_URL_PREFIX + lang + WIKI_URL_SUFFIX); 
      }
      
      m_interLinks = new HashMap<String, String>();
      m_origLang = origLang;
      m_interLang = interLang;
      m_outPath = outPath;
      m_locale = new Locale(m_origLang);
      m_english = LANG_ENGLISH.equals(m_origLang);
    }
    
    public boolean process(WikiArticle article)
    {      
      try
      {
        m_wikiModel = new WikiModel(m_config, m_locale, "${image}", "${title}");        
        m_interLinks.clear();
        
        // TODO: Pasing twice: once to gather language information, and another time to produce plain text
        m_wikiModel.parseEvents(this, article.getText());
        String plainStr = m_wikiModel.render(new PlainTextConverter(), article.getText());
        String englishTitle = m_english ? article.getTitle() : translateTitleTo("en");
        
        if (plainStr != null && (plainStr = finalCleanUp(plainStr)).length() > 10 && 
            ((m_english && hasAnyTranslations()) || (!m_english && hasTranslationIn(m_interLang))) && 
            englishTitle != null && englishTitle.trim().length() > 0)
        {
          englishTitle = englishTitle.replaceAll("/", "-");
          
          if (englishTitle.startsWith("."))
          { englishTitle = OTHER_PREFIX + englishTitle;
          }
          
          writeOut(m_outPath, englishTitle, m_origLang, plainStr, false);
          writeOut(m_outPath, englishTitle, m_origLang, article.getText(), true);
        }
      }
      catch (Exception e)
      {
        System.err.println("Failed processing or writing page " + article.getTitle() + " in " + m_origLang + "(" + e.toString() + ")");
      }
      
      return true;
    }
    
    /**
     * Store all of the interwiki language links.
     */
    public synchronized void onWikiLink(char[] src, int rawStart, int rawEnd, String suffix) 
    {
      
      String linkStr = new String(src).substring(rawStart, rawEnd);
      int colonIndex = linkStr.indexOf(':');
      
      if (colonIndex != (-1))
      {
        String nameSpace = linkStr.substring(0, colonIndex);
        String title = linkStr.substring(colonIndex + 1);
        
        // Maybe a bit of an overkill
        if ((linkStr.charAt(colonIndex + 1) != ':') &&
            (linkStr.charAt(colonIndex + 1) != '=') &&
            !m_wikiModel.isCategoryNamespace(nameSpace) &&
            m_wikiModel.isInterWiki(nameSpace) && title.length() > 0)
        {
          m_interLinks.put(nameSpace, title);
        }
      }
    }

    protected synchronized boolean hasAnyTranslations()
    {
      boolean yes = false;
    
      if (m_interLinks.size() > 0)
      {
        for (String lang : WIKI_LANGS)
        {
          if (yes = m_interLinks.containsKey(lang))
          {
            break;
          }
        }
      }
      
      return yes;
    }
    
    protected synchronized boolean hasTranslationIn(String language)
    {
      return m_interLinks.containsKey(language);
    }
        
    protected synchronized String translateTitleTo(String language)
    {
      return m_interLinks.get(language);
    }
    
    protected void writeOut(String path, String name, String lang, String plainText, boolean rawWiki) throws IOException
    {
      String firstLetter = name.substring(0, 1).toLowerCase();
      
      if (!firstLetter.matches("[a-z0-9]"))
      { firstLetter = OTHER_PREFIX;
      }
      
      File f = new File(path + "/" + firstLetter);
      
      if (!f.exists())
      { f.mkdir();
      }
      
      f = new File(path + "/" + firstLetter + "/" + name + (rawWiki ? ".wiki." : ".") + lang);
      
      if (f.exists())
      {f.delete();
      }
      
      BufferedWriter out = new BufferedWriter(new FileWriter(f));
      out.write(plainText);
      out.close();
    }
    
    protected String finalCleanUp(String str)
    {
      String text = str.replaceAll("\\{\\{.*?\\}\\}", " ");
      text = text.replaceAll("[ \t]+\n", "\n");
      text = text.replaceAll("\n\n[\n]+", "\n\n").trim();

      return text;
    }
    
    protected HashMap<String, String> m_interLinks;
    protected Configuration m_config;
    protected WikiModel m_wikiModel;
    protected String m_origLang;
    protected String m_interLang;
    protected String m_outPath;
    protected Locale m_locale;
    protected boolean m_english;
  }
}
