/*
 * File   : $Source: /alkacon/cvs/AlkaconDiff/src/com/alkacon/diff/I_DiffConfiguration.java,v $
 * Date   : $Date: 2005/10/28 08:55:38 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.alkacon.diff;

/**
 * Configurates a Diff operation.<p>
 * 
 * Provides localized messages and number of lines to show before skipping.
 */
interface I_DiffConfiguration {

    /**
     * Returns the number of lines to show before skipping.<p>
     * 
     * Use <code>-1</code> to show all lines.<p>
     * 
     * @return the number of lines to show before skipping
     */
    int getLinesBeforeSkip();

    /**
     * Returns a localized message like '({0} equal lines skipped)'.<p>
     * 
     * @param lines the number of skipped lines
     * 
     * @return a localized message
     */
    String getMessageEqualLinesSkipped(int lines);
}