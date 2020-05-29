package tmf.host

import org.apache.commons.lang.StringUtils
import tmf.host.util.LinkHelper

class TmfTagLib {
    static defaultEncodeAs = [taglib: 'raw']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "tmf"



    Closure createLink = { attrs, body ->
        String linkHref = "<unknown>";

        if( StringUtils.isBlank(attrs.format) )
            attrs.format = "html";

        if( attrs.td == null && attrs.tip == null && attrs.subIdentifier == null )
            throw new UnsupportedOperationException("The tmf:createLink taglib requires either the td, tip or subIdentifier attribute be given.");

        if( attrs.td ){
            linkHref = LinkHelper.getLink(request, attrs.td, attrs.format);
        }else if( attrs.tip ){
            linkHref = LinkHelper.getLink(request, attrs.tip, attrs.format);
        }else if( attrs.subIdentifier ){
            linkHref = LinkHelper.getLink(request, attrs.subIdentifier, attrs.format);
        }

        out << "<a href=\"${linkHref}\">\n"
        out << body()
        out << "</a>\n"
    }

    Closure createHref = { attrs, body ->

    }

    Closure treeTableRowForTaxonomyTerm = {attrs, body ->
        if( attrs.term == null ){
            throw new UnsupportedOperationException("Missing required tmf:treeTableRowForTaxonomyTerm attribute 'term'!");
        }

        TaxonomyTerm term = attrs.term;
        out << _recursivelyBuildTreeTableStructure(term);
    }



    private String _recursivelyBuildTreeTableStructure(TaxonomyTerm term){
        String row = '<tr data-tt-id="'+term.id+'"';
        if( term.parent )
            row += ' data-tt-parent-id="'+term.parent.id+'"';
        row += ">";
        row += "<td>${term.name}</td>";
        row += "</tr>\n";

        if( term.getChildren().size() > 0 ){
            for(TaxonomyTerm child : term.getChildren()){
                row += _recursivelyBuildTreeTableStructure(child);
            }
        }

        return row;
    }
}
