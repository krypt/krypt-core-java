/***** BEGIN LICENSE BLOCK *****
* Version: CPL 1.0/GPL 2.0/LGPL 2.1
*
* The contents of this file are subject to the Common Public
* License Version 1.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.eclipse.org/legal/cpl-v10.html
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.
*
* Copyright (C) 2011-2013
* Hiroshi Nakamura <nahi@ruby-lang.org>
* Martin Bosslet <Martin.Bosslet@gmail.com>
*
* Alternatively, the contents of this file may be used under the terms of
* either of the GNU General Public License Version 2 or later (the "GPL"),
* or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
* in which case the provisions of the GPL or the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of either the GPL or the LGPL, and not to allow others to
* use your version of this file under the terms of the CPL, indicate your
* decision by deleting the provisions above and replace them with the notice
* and other provisions required by the GPL or the LGPL. If you do not delete
* the provisions above, a recipient may use your version of this file under
* the terms of any one of the CPL, the GPL or the LGPL.
 */
package impl.krypt.asn1;


/**
 * 
 * @author <a href="mailto:Martin.Bosslet@gmail.com">Martin Bosslet</a>
 */
public enum TagClass {
    
    UNIVERSAL       (Masks.UNIVERSAL_MASK),
    APPLICATION     (Masks.APPLICATION_MASK),
    CONTEXT_SPECIFIC(Masks.CONTEXT_SPECIFIC_MASK),
    PRIVATE         (Masks.PRIVATE_MASK);
    
    TagClass(byte mask) {
        this.mask = mask;
    }
    
    public static TagClass forName(String name) {
        if ("IMPLICIT".equals(name))
            return CONTEXT_SPECIFIC;
        if ("EXPLICIT".equals(name))
            return CONTEXT_SPECIFIC;
        return TagClass.valueOf(name);
    }
    
    private final byte mask;
    
    public byte getMask() {
        return mask;
    }
    
    public static TagClass of(byte b) {
        switch (b) {
            case Masks.UNIVERSAL_MASK:
                return UNIVERSAL;
            case Masks.APPLICATION_MASK:
                return APPLICATION;
            case Masks.CONTEXT_SPECIFIC_MASK:
                return CONTEXT_SPECIFIC;
            case Masks.PRIVATE_MASK:
                return PRIVATE;
            default:
                throw new IllegalArgumentException("Unknown tag class: " + b);
        }
    }
    
    private static class Masks {
        static final byte UNIVERSAL_MASK        = (byte)0x00;
        static final byte APPLICATION_MASK      = (byte)0x40;
        static final byte CONTEXT_SPECIFIC_MASK = (byte)0x80;
        static final byte PRIVATE_MASK          = (byte)0xc0;
    }

}
