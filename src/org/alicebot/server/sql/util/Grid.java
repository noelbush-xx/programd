/*
 * Grid.java
 */

package org.alicebot.server.sql.util;
import java.awt.*;
import java.util.Vector;
import javax.swing.*;

public class Grid extends JPanel {
  // drawing
  public Dimension dMinimum;
  public Font fFont;
  public FontMetrics fMetrics;
  public Graphics gImage;
  public Image iImage;
  // height / width
  public int iWidth,iHeight;
  public int iRowHeight,iFirstRow;
  public int iGridWidth,iGridHeight;
  public int iX,iY;
  // data
  public String sColHead[];
  public Vector vData;
  public int iColWidth[];
  public int iColCount,iRowCount;
  // scrolling
  public Scrollbar sbHoriz,sbVert;
  public int iSbWidth,iSbHeight;
  public boolean bDrag;
  public int iXDrag,iColDrag;

  public Grid() {
    super();
    fFont=new Font("Dialog",Font.PLAIN,12);
    setLayout(null);
    sbHoriz=new Scrollbar(Scrollbar.HORIZONTAL);
    add(sbHoriz);
    sbVert=new Scrollbar(Scrollbar.VERTICAL);
    add(sbVert);
  }
  public String[] getHead() {
    return sColHead;
  }
  public Vector getData() {
    return vData;
  }
  public void setMinimumSize(Dimension d) {
    dMinimum=d;
  }
  public void reshape(int x,int y,int w,int h) {
    super.reshape(x,y,w,h);
    iSbHeight=sbHoriz.getPreferredSize().height;
    iSbWidth=sbVert.getPreferredSize().width;
    iHeight=h-iSbHeight;
    iWidth=w-iSbWidth;
    sbHoriz.reshape(0,iHeight,iWidth,iSbHeight);
    sbVert.reshape(iWidth,0,iSbWidth,iHeight);
    adjustScroll();
    iImage=null;
    repaint();
  }
  public void setHead(String head[]) {
    vData=new Vector();
    iColCount=head.length;
    sColHead=new String[iColCount];
    iColWidth=new int[iColCount];
    for(int i=0;i<iColCount;i++) {
      sColHead[i]=head[i];
      iColWidth[i]=100;
    }
    iRowCount=0;
    iRowHeight=0;
  }
  public void addRow(String data[]) {
    if(data.length!=iColCount) {
      return;
    }
    String row[]=new String[iColCount];
    for(int i=0;i<iColCount;i++) {
      row[i]=data[i];
    }
    vData.addElement(row);
    iRowCount++;
  }
  public void update() {
    adjustScroll();
    repaint();
  }
  public void adjustScroll() {
    if(iRowHeight==0) {
      return;
    }
    int w=0;
    for(int i=0;i<iColCount;i++) {
      w+=iColWidth[i];
    }
    iGridWidth=w;
    iGridHeight=iRowHeight*(iRowCount+1);
    sbHoriz.setValues(iX,iWidth,0,iGridWidth);
    int v=iY/iRowHeight,h=iHeight/iRowHeight;
    sbVert.setValues(v,h,0,iRowCount+1);
    iX=sbHoriz.getValue();
    iY=iRowHeight*sbVert.getValue();
  }
  public boolean handleEvent(Event e) {
    switch(e.id) {
    case Event.SCROLL_LINE_UP:
    case Event.SCROLL_LINE_DOWN:
    case Event.SCROLL_PAGE_UP:
    case Event.SCROLL_PAGE_DOWN:
    case Event.SCROLL_ABSOLUTE:
      iX=sbHoriz.getValue();
      iY=iRowHeight*sbVert.getValue();
      repaint();
      return true;
    }
    return super.handleEvent(e);
  }
  public void paint(Graphics g) {
    if(g==null) {
      return;
    }
    if(iWidth<=0 || iHeight<=0) {
      return;
    }
    g.setColor(SystemColor.control);
    g.fillRect(iWidth,iHeight,iSbWidth,iSbHeight);
    if(iImage==null) {
      iImage=createImage(iWidth,iHeight);
      gImage=iImage.getGraphics();
      gImage.setFont(fFont);
      if(fMetrics==null) {
        fMetrics=gImage.getFontMetrics();
      }
    }
    if(iRowHeight==0) {
      iRowHeight=getMaxHeight(fMetrics);
      for(int i=0;i<iColCount;i++) {
        calcAutoWidth(i);
      }
      adjustScroll();
    }
    gImage.setColor(Color.white);
    gImage.fillRect(0,0,iWidth,iHeight);
    gImage.setColor(Color.darkGray);
    gImage.drawLine(0,iRowHeight,iWidth,iRowHeight);
    int x=-iX;
    for(int i=0;i<iColCount;i++) {
      int w=iColWidth[i];
      gImage.setColor(SystemColor.control);
      gImage.fillRect(x+1,0,w-2,iRowHeight);
      gImage.setColor(Color.black);
      gImage.drawString(sColHead[i],x+2,iRowHeight-5);
      gImage.setColor(Color.darkGray);
      gImage.drawLine(x+w-1,0,x+w-1,iRowHeight-1);
      gImage.setColor(Color.white);
      gImage.drawLine(x+w,0,x+w,iRowHeight-1);
      x+=w;
    }
    gImage.setColor(SystemColor.control);
    gImage.fillRect(0,0,1,iRowHeight);
    gImage.fillRect(x+1,0,iWidth-x,iRowHeight);
    gImage.drawLine(0,0,0,iRowHeight-1);
    int y=iRowHeight+1-iY;
    int j=0;
    while(y<iRowHeight+1) {
      j++;
      y+=iRowHeight;
    }
    iFirstRow=j;
    y=iRowHeight+1;
    for(;y<iHeight && j<iRowCount;j++,y+=iRowHeight) {
      x=-iX;
      for(int i=0;i<iColCount;i++) {
        int w=iColWidth[i];
        Color b=Color.white,t=Color.black;
        gImage.setColor(b);
        gImage.fillRect(x,y,w-1,iRowHeight-1);
        gImage.setColor(t);
        gImage.drawString(getDisplay(i,j),x+2,y+iRowHeight-5);
        gImage.setColor(Color.lightGray);
        gImage.drawLine(x+w-1,y,x+w-1,y+iRowHeight-1);
        gImage.drawLine(x,y+iRowHeight-1,x+w-1,y+iRowHeight-1);
        x+=w;
      }
      gImage.setColor(Color.white);
      gImage.fillRect(x,y,iWidth-x,iRowHeight-1);
    }
    g.drawImage(iImage,0,0,this);
  }
  public void update(Graphics g) {
    paint(g);
  }
  public boolean mouseMove(Event e,int x,int y) {
    if(y<=iRowHeight) {
      int xb=x;
      x+=iX-iGridWidth;
      int i=iColCount-1;
      for(;i>=0;i--) {
        if(x>-7 && x<7) {
          break;
        }
        x+=iColWidth[i];
      }
      if(i>=0) {
        if(!bDrag) {
          setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
          bDrag=true;
          iXDrag=xb-iColWidth[i];
          iColDrag=i;
        }
        return true;
      }
    }
    return mouseExit(e,x,y);
  }
  public boolean mouseDrag(Event e,int x,int y) {
    if(bDrag && x<iWidth) {
      int w=x-iXDrag;
      if(w<0) {
        w=0;
      }
      iColWidth[iColDrag]=w;
      adjustScroll();
      repaint();
    }
    return true;
  }
  public boolean mouseExit(Event e,int x,int y) {
    if(bDrag) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      bDrag=false;
    }
    return true;
  }
  public Dimension preferredSize() {
    return minimumSize();
  }
  public Dimension getPreferredSize() {
    return minimumSize();
  }
  public Dimension getMinimumSize() {
    return minimumSize();
  }
  public Dimension minimumSize() {
    return dMinimum;
  }

  public void calcAutoWidth(int i) {
    int w=10;
    w=Math.max(w,fMetrics.stringWidth(sColHead[i]));
    for(int j=0;j<iRowCount;j++) {
      String s[]=(String[])(vData.elementAt(j));
      w=Math.max(w,fMetrics.stringWidth(s[i]));
    }
    iColWidth[i]=w+6;
  }
  public String getDisplay(int x,int y) {
    return (((String[])(vData.elementAt(y)))[x]);
  }
  public String get(int x,int y) {
    return (((String[])(vData.elementAt(y)))[x]);
  }
  public static int getMaxHeight(FontMetrics f) {
    return f.getHeight()+4;
  }
}
