package io.dico.dicore.command.chat.help;

public class PageBorders {
    private final IPageBorder header, footer;
    
    public PageBorders(IPageBorder header, IPageBorder footer) {
        this.header = header;
        this.footer = footer;
    }
    
    public IPageBorder getHeader() {
        return header;
    }
    
    public IPageBorder getFooter() {
        return footer;
    }
    
    public static IPageBorder simpleBorder(String... lines) {
        return new SimplePageBorder(lines);
    }
    
    public static IPageBorder disappearingBorder(int pageNum, String... lines) {
        return new DisappearingPageBorder(pageNum, lines);
    }
    
    static class SimplePageBorder extends SimpleHelpComponent implements IPageBorder {
        private final String replacedSequence;
        
        public SimplePageBorder(String replacedSequence, String... lines) {
            super(lines);
            this.replacedSequence = replacedSequence;
        }
        
        public SimplePageBorder(String... lines) {
            super(lines);
            this.replacedSequence = "%pageCount%";
        }
        
        @Override
        public void setPageCount(int pageCount) {
            String[] lines = this.lines;
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].replace(replacedSequence, Integer.toString(pageCount));
            }
        }
        
    }
    
    static class DisappearingPageBorder extends SimpleHelpComponent implements IPageBorder {
        private final int pageNum;
        
        public DisappearingPageBorder(int pageNum, String... lines) {
            super(lines);
            this.pageNum = pageNum;
        }
        
        @Override
        public void setPageCount(int pageCount) {
            if (pageCount == pageNum) {
                lines = new String[0];
            }
        }
        
    }
    
}
