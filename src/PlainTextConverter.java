import info.bliki.htmlcleaner.ContentToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.PTag;
import info.bliki.wiki.tags.RefTag;
import info.bliki.wiki.tags.WPATag;
import info.bliki.wiki.tags.WPTag;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlainTextConverter implements ITextConverter {
  boolean fNoLinks;

  public PlainTextConverter(boolean noLinks)
  {
    this.fNoLinks = noLinks;
  }

  public PlainTextConverter()
  {  
    this(true);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void nodesToText(List<? extends Object> nodes, Appendable resultBuf, IWikiModel model) throws IOException
  {
    StringBuilder strBld = new StringBuilder();
    
    if (nodes != null && !nodes.isEmpty()) 
    {
      try
      {
        int level = model.incrementRecursionLevel();

        if (level > Configuration.RENDERER_RECURSION_LIMIT)
        {
          resultBuf.append("Error - recursion limit exceeded rendering tags in PlainTextConverter#nodesToText().");
          return;
        }
        Iterator<? extends Object> childrenIt = nodes.iterator();
        
        while (childrenIt.hasNext())
        {
          Object item = childrenIt.next();
          if (item != null) 
          {
            if (item instanceof List)
            {
              nodesToText((List) item, strBld, model);
            } 
            else if (item instanceof ContentToken)
            {
              ContentToken contentToken = (ContentToken) item;
              String content = contentToken.getContent();
              content = Utils.escapeXml(content, true, true, true);
              strBld.append(content);
            }
            else if (item instanceof WPList)
            {
              ((WPList)item).renderPlainText(this, strBld, model);
            } 
            else if (item instanceof WPTable)
            {
              ((WPTable)item).renderPlainText(this, strBld, model);
            }
            else if (item instanceof HTMLTag)
            {
              getBodyStr(((HTMLTag)item), strBld);
            }
            else if (item instanceof TagNode)
            {
              TagNode node = (TagNode) item;
              Map<String, Object> map = node.getObjectAttributes();
              if (map != null && map.size() > 0)
              { }
              else
              {
                getBodyStr(node, strBld);
              }
            }
          }
        }
      } 
      finally 
      {
        model.decrementRecursionLevel();
      }
    }
    
    resultBuf.append(strBld);
  }
  
  public void getBodyStr(TagNode tag, Appendable buf) throws IOException
  {
    List<Object> children = tag.getChildren();
    
    if (tag instanceof RefTag)
    {
      // Do nothing
    }
    else if ((tag instanceof WPATag) && isUnaliasedLink((WPATag)tag))
    {
      String name = ((ContentToken)tag.getChildren().get(0)).getContent();
      int colonIdx = name.lastIndexOf(":");
      
      if (colonIdx != -1)
      {
        buf.append(name.substring(colonIdx + 1));
      }
      else
      {
        buf.append(name);
      }
    }
    else if (children.size() > 0)
    {
      for (int i = 0; i < children.size(); i++)
      {
        if (children.get(i) instanceof ContentToken)
        {
          buf.append(((ContentToken) children.get(i)).getContent());
        }
        else if (children.get(i) instanceof HTMLTag)
        {
          getBodyStr((HTMLTag) children.get(i), buf);
        }
      }
    }
    
    if (tag instanceof PTag)
    {
      buf.append("\n\n"); 
    }
    else if (tag instanceof WPTag)
    {      
      String name = tag.getName();
      
      if (name != null && name.startsWith("h"))
      { buf.append("\n\n");
      }
    }
  }
  
  public boolean noLinks() 
  {
    return fNoLinks;
  }
  
  public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, Appendable resultBuffer, IWikiModel model) throws IOException 
  {}

  protected boolean isUnaliasedLink(WPATag w)
  {
    boolean empty = false;
    Map<String, Object> attribs = w.getObjectAttributes();
    List<Object> children = w.getChildren();
    Object child, link;
      
    if (attribs != null && ((link = attribs.get("wikilink")) != null) &&
        children != null && children.size() == 1 && 
        ((child = children.get(0)) instanceof ContentToken))
    {        
      empty = link.toString().equals(((ContentToken)child).getContent());
    }
  
    return empty;
  }
}