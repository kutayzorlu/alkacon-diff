/*
 * File   : $Source: /alkacon/cvs/AlkaconDiff/src/com/alkacon/diff/Diff.java,v $
 * Date   : $Date: 2005/10/20 07:32:38 $
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

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;

/**
 * Calculates the difference from two given input sources.<p>
 */
public final class Diff {

    /**
     * Hides the public contructor.<p> 
     */
    private Diff() {

        // empty
    }

    /**
     * Diffs two texts, outputting the result to the specified DiffOutput instance.<p>
     * 
     * @param text1 the first text to compare 
     * @param text2 the second text to compare
     * @param output the result of the diff is written on this instance
     * @param skipLineCount if this many lines are equals they are skipped, specify -1 to include complete text
     * 
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *      methods on DiffOutput can
     */
    public static void diff(String text1, String text2, I_DiffOutput output, int skipLineCount) throws Exception {

        TextComparator leftComparator = new TextComparator(text1);
        TextComparator rightComparator = new TextComparator(text2);

        RangeDifference[] differences = RangeDifferencer.findDifferences(leftComparator, rightComparator);

        int pos = 0; // the position (line) in the left input
        if (differences.length > 0) {
            int diffIndex = 0;

            int leftLineCount = leftComparator.getRangeCount();
            while (diffIndex < differences.length && pos < leftLineCount) {
                RangeDifference diff = differences[diffIndex];

                if (diff.kind() == RangeDifference.CHANGE) {
                    int nextChangedLine = diff.leftStart();

                    // output contextLineCount number of lines (if available) or all lines if contextLineCount == -1
                    if (pos != 0) { // at start of file, skip immediately to first changes
                        int beginContextEndPos = pos + skipLineCount;
                        while ((pos < beginContextEndPos || skipLineCount == -1) && pos < nextChangedLine) {
                            output.startLine(DiffLineType.UNCHANGED);
                            output.addUnchangedText(leftComparator.getLine(pos));
                            output.endLine();
                            pos++;
                        }
                    }

                    // skip a number of lines
                    if (skipLineCount >= 0) {
                        int endContextStartPos = nextChangedLine - skipLineCount;
                        if (endContextStartPos > pos + 1) { // the +1 is to avoid skipping just one line
                            output.skippedLines(endContextStartPos - pos);
                            pos = endContextStartPos;
                        }
                    }

                    // output contextLineCount number of lines
                    while (pos < nextChangedLine) {
                        output.startLine(DiffLineType.UNCHANGED);
                        output.addUnchangedText(leftComparator.getLine(pos));
                        output.endLine();
                        pos++;
                    }

                    StringBuffer leftBlock = null;
                    StringBuffer rightBlock = null;
                    if (diff.leftLength() > 0 && diff.rightLength() > 0) {
                        leftBlock = concatLines(leftComparator, diff.leftStart(), diff.leftLength());
                        rightBlock = concatLines(rightComparator, diff.rightStart(), diff.rightLength());
                    }

                    if (leftBlock == null) {
                        for (int i = 0; i < diff.leftLength(); i++) {
                            int currentLine = diff.leftStart() + i;
                            output.startLine(DiffLineType.REMOVED);
                            output.addUnchangedText(leftComparator.getLine(currentLine));
                            output.endLine();
                        }
                    } else {
                        diffBlock(leftBlock, rightBlock, output, DiffLineType.REMOVED);
                    }

                    if (leftBlock == null) {
                        for (int i = 0; i < diff.rightLength(); i++) {
                            int currentLine = diff.rightStart() + i;
                            output.startLine(DiffLineType.ADDED);
                            output.addUnchangedText(rightComparator.getLine(currentLine));
                            output.endLine();
                        }
                    } else {
                        diffBlock(rightBlock, leftBlock, output, DiffLineType.ADDED);
                    }
                }

                pos = differences[diffIndex].leftEnd();
                diffIndex++;
            }

            // output any remaining lines
            int endPos = pos;
            while (pos < leftLineCount && (skipLineCount == -1 || pos < endPos + skipLineCount)) {
                output.startLine(DiffLineType.UNCHANGED);
                output.addUnchangedText(leftComparator.getLine(pos));
                output.endLine();
                pos++;
            }
            if (pos < leftLineCount) {
                output.skippedLines(leftLineCount - pos);
            }
        }
    }

    /**
     * Returns the diff of the given two input texts in HTML format.<p>
     * 
     * All lines of the input are included in the output.<p>
     * 
     * @param text1 the first text to compare 
     * @param text2 the second text to compare
     * 
     * @return the diff of the given two input texts in HTML format
     * 
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *      methods on DiffOutput can
     */
    public static String diffAsHtml(String text1, String text2) throws Exception {

        return diffAsHtml(text1, text2, -1);
    }

    /**
     * Returns the diff of the given two input texts in HTML format.<p>
     * 
     * @param text1 the first text to compare 
     * @param text2 the second text to compare
     * @param skipLineCount if this many lines are equals they are skipped, specify -1 to include complete text
     * 
     * @return the diff of the given two input texts in HTML format
     * 
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *      methods on DiffOutput can
     */
    public static String diffAsHtml(String text1, String text2, int skipLineCount) throws Exception {

        StringWriter writer = new StringWriter(4096);
        diffAsHtml(text1, text2, writer, skipLineCount);
        return writer.toString();
    }

    /**
     * Diffs two texts, outputting the result as HTML to the specified writer instance.<p>
     * 
     * @param text1 the first text to compare 
     * @param text2 the second text to compare
     * @param writer the result of the diff is written on this writer
     * @param skipLineCount if this many lines are equals they are skipped, specify -1 to include complete text
     * 
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *      methods on DiffOutput can
     */
    public static void diffAsHtml(String text1, String text2, Writer writer, int skipLineCount) throws Exception {

        XmlSaxWriter saxWriter = new XmlSaxWriter(writer);
        I_DiffOutput output = new HtmlDiffOutput(saxWriter);
        diff(text1, text2, output, skipLineCount);
    }

    /**
     * Returns the diff of the given two input texts in plain text format.<p>
     * 
     * All lines of the input are included in the output.<p>
     * 
     * @param text1 the first text to compare 
     * @param text2 the second text to compare
     * 
     * @return the diff of the given two input texts in plain text format
     * 
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *      methods on DiffOutput can
     */
    public static String diffAsText(String text1, String text2) throws Exception {

        return diffAsText(text1, text2, -1);
    }

    /**
     * Returns the diff of the given two input texts in plain text format.<p>
     * 
     * @param text1 the first text to compare 
     * @param text2 the second text to compare
     * @param skipLineCount if this many lines are equals they are skipped, specify -1 to include complete text
     * 
     * @return the diff of the given two input texts in plain text format
     * 
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *      methods on DiffOutput can
     */
    public static String diffAsText(String text1, String text2, int skipLineCount) throws Exception {

        StringWriter writer = new StringWriter(4096);
        diffAsText(text1, text2, writer, skipLineCount);
        return writer.toString();
    }

    /**
     * Diffs two texts, outputting the result as  plain text to the specified writer instance.<p>
     * 
     * @param text1 the first text to compare 
     * @param text2 the second text to compare
     * @param writer the result of the diff is written on this writer
     * @param skipLineCount if this many lines are equals they are skipped, specify -1 to include complete text
     * 
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *      methods on DiffOutput can
     */
    public static void diffAsText(String text1, String text2, Writer writer, int skipLineCount) throws Exception {

        I_DiffOutput output = new TextDiffOutput(writer, true);
        diff(text1, text2, output, skipLineCount);
    }

    private static StringBuffer concatLines(TextComparator comparator, int start, int count) {

        int totalLinesLength = 0;
        for (int i = 0; i < count; i++) {
            totalLinesLength += comparator.getLine(start + i).length() + 1;
        }

        StringBuffer result = new StringBuffer(totalLinesLength);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                result.append("\n");
            }
            result.append(comparator.getLine(start + i));
        }

        return result;
    }

    private static void diffBlock(
        StringBuffer block1,
        StringBuffer block2,
        I_DiffOutput output,
        DiffLineType diffLineType) throws Exception {

        BlockComparator leftBlockComparator = new BlockComparator(block1);
        BlockComparator rightBlockComparator = new BlockComparator(block2);
        RangeDifference[] lineDiffs = RangeDifferencer.findDifferences(leftBlockComparator, rightBlockComparator);

        int pos = 0;
        RangeDifference diff = null;
        output.startLine(diffLineType);

        for (int i = 0; i < lineDiffs.length; i++) {
            diff = lineDiffs[i];

            int left = diff.leftStart();
            if (pos < left) {
                String[] strings = leftBlockComparator.substringSplitted(pos, left);
                for (int d = 0; d < strings.length; d++) {
                    if (strings[d].equals("\n")) {
                        output.endLine();
                        output.startLine(diffLineType);
                    } else {
                        output.addUnchangedText(strings[d]);
                    }
                }
            }

            if (diff.leftLength() > 0) {
                String[] strings = leftBlockComparator.substringSplitted(left, diff.leftEnd());
                for (int d = 0; d < strings.length; d++) {
                    if (strings[d].equals("\n")) {
                        output.endLine();
                        output.startLine(diffLineType);
                    } else {
                        output.addChangedText(strings[d]);
                    }
                }
            }

            pos = diff.leftEnd();
        }

        if (diff == null || diff.leftEnd() < leftBlockComparator.getRangeCount()) {
            int start = 0;
            if (diff != null) {
                start = diff.leftEnd();
            }
            String[] strings = leftBlockComparator.substringSplitted(start);
            for (int d = 0; d < strings.length; d++) {
                if (strings[d].equals("\n")) {
                    output.endLine();
                    output.startLine(diffLineType);
                } else {
                    output.addUnchangedText(strings[d]);
                }
            }
            output.endLine();
        } else {
            output.endLine();
        }
    }
}