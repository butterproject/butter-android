/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.subs;

public class Style {

    private static int styleCounter;

    /**
     * Constructor that receives a String to use a its identifier
     *
     * @param styleName = identifier of this style
     */
    Style(String styleName) {
        this.iD = styleName;
    }

    /**
     * Constructor that receives a String with the new styleName and a style to copy
     *
     * @param styleName Style name
     * @param style Style object
     */
    Style(String styleName, Style style) {
        this.iD = styleName;
        this.font = style.font;
        this.fontSize = style.fontSize;
        this.color = style.color;
        this.backgroundColor = style.backgroundColor;
        this.textAlign = style.textAlign;
        this.italic = style.italic;
        this.underline = style.underline;
        this.bold = style.bold;

    }

    /* ATTRIBUTES */
    String iD;
    String font;
    String fontSize;
    /**
     * colors are stored as 8 chars long RGBA
     */
    String color;
    String backgroundColor;
    String textAlign = "";

    boolean italic;
    boolean bold;
    boolean underline;

	/* METHODS */

    /**
     * To get the string containing the hex value to put into color or background color
     *
     * @param format supported: "name", "&HBBGGRR", "&HAABBGGRR", "decimalCodedBBGGRR", "decimalCodedAABBGGRR"
     * @param value  RRGGBBAA string
     */
    static String getRGBValue(String format, String value) {
        String color = null;
        if (format.equalsIgnoreCase("name")) {
            //standard color format from W3C
            switch (value) {
                case "transparent":
                    color = "00000000";
                    break;
                case "black":
                    color = "000000ff";
                    break;
                case "silver":
                    color = "c0c0c0ff";
                    break;
                case "gray":
                    color = "808080ff";
                    break;
                case "white":
                    color = "ffffffff";
                    break;
                case "maroon":
                    color = "800000ff";
                    break;
                case "red":
                    color = "ff0000ff";
                    break;
                case "purple":
                    color = "800080ff";
                    break;
                case "fuchsia":
                    color = "ff00ffff";
                    break;
                case "magenta":
                    color = "ff00ffff ";
                    break;
                case "green":
                    color = "008000ff";
                    break;
                case "lime":
                    color = "00ff00ff";
                    break;
                case "olive":
                    color = "808000ff";
                    break;
                case "yellow":
                    color = "ffff00ff";
                    break;
                case "navy":
                    color = "000080ff";
                    break;
                case "blue":
                    color = "0000ffff";
                    break;
                case "teal":
                    color = "008080ff";
                    break;
                case "aqua":
                    color = "00ffffff";
                    break;
                case "cyan":
                    color = "00ffffff ";
                    break;
            }
        } else if (format.equalsIgnoreCase("&HBBGGRR")) {
            //hex format from SSA
            color = value.substring(6) +
                    value.substring(4, 5) +
                    value.substring(2, 3) +
                    "ff";
        } else if (format.equalsIgnoreCase("&HAABBGGRR")) {
            //hex format from ASS
            color = value.substring(8) +
                    value.substring(6, 7) +
                    value.substring(4, 5) +
                    value.substring(2, 3);
        } else if (format.equalsIgnoreCase("decimalCodedBBGGRR")) {
            //normal format from SSA
            color = Integer.toHexString(Integer.parseInt(value));
            //any missing 0s are filled in
            while (color.length() < 6) color = "0" + color;
            //order is reversed
            color = color.substring(4) + color.substring(2, 4) + color.substring(0, 2) + "ff";
        } else if (format.equalsIgnoreCase("decimalCodedAABBGGRR")) {
            //normal format from ASS
            color = Long.toHexString(Long.parseLong(value));
            //any missing 0s are filled in
            while (color.length() < 8) color = "0" + color;
            //order is reversed
            color = color.substring(6) + color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
        }
        return color;
    }

    static String defaultID() {
        return "default" + styleCounter++;
    }

    @Override
    public String toString() {
        return "Style{" +
                "id='" + iD + '\'' +
                ", font='" + font + '\'' +
                ", fontSize='" + fontSize + '\'' +
                ", color='" + color + '\'' +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", textAlign='" + textAlign + '\'' +
                ", italic=" + italic +
                ", bold=" + bold +
                ", underline=" + underline +
                '}';
    }

}
