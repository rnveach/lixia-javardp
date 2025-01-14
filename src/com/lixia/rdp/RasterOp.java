/* RasterOp.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.1.1.1 $
 * Author: $Author: suvarov $
 * Date: $Date: 2007/03/08 00:26:20 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Set of operations used in displaying raster graphics
 */
// Created on 01-Jul-2003

package com.lixia.rdp;


import org.apache.log4j.Logger;

public class RasterOp {
	static Logger logger = Logger.getLogger(RdesktopJPanel.class);

	private void ropInvert(WrappedImage biDst, int[] dest, int width, int x, int y, int cx, int cy) {
		int mask = Options.bpp_mask;
		int pdest = (y * width + x);
		for (int i = 0; i < cy; i++) {
			for (int j = 0; j < cx; j++) {
                if(biDst != null){
                    int c = biDst.getRGB(x+j,y+i);
                    biDst.setRGB(x+j,y+i,~c & mask);
                }else dest[pdest] = (~dest[pdest]) & mask;
				pdest++;
			}
			pdest += (width - cx);
		}
	}

	private void ropClear(WrappedImage biDst, int width, int x, int y, int cx, int cy) {

	    for(int i = x; i < x+cx; i++){
	            for(int j = y; j < y+cy; j++)
	                biDst.setRGB(i,j,0);
	    }
	}

	private void ropSet(WrappedImage biDst, int width, int x, int y, int cx, int cy) {
        
        int mask = Options.bpp_mask;
        
            for(int i = x; i < x+cx; i++){
                for(int j = y; j < y+cy; j++)
                    biDst.setRGB(i,j,mask);
            }

    }

	private void ropCopy(WrappedImage biDst, int dstwidth, int x, int y, int cx, int cy,
			int[] src, int srcwidth, int srcx, int srcy) {

		if (src == null) { // special case - copy to self
            int[] imgSec = null;
            biDst.getGraphics().copyArea(srcx,srcy,cx,cy,x-srcx,y-srcy);
		}
		else if (srcy > 0)
	    {
	      int[] arrayOfInt = new int[src.length];
	      int j = srcy * srcwidth;
	      int k = Math.min(srcwidth * srcwidth, arrayOfInt.length);
	      int i;
	      for (i = 0; i < k - j; ++i)
	        arrayOfInt[i] = src[(i + j)];
	      while (i < arrayOfInt.length)
	      {
	        arrayOfInt[i] = 0;
	        ++i;
	      }
	      biDst.setRGB(x, y, cx, cy, arrayOfInt, srcx, srcwidth);
	    }
		else {
                biDst.setRGB(
                        x,
                        y,
                        cx,
                        cy,
                        src,
                        srcx,
                        srcwidth
                    );
		}
	}

    /**
     * Perform an operation on a rectangular area of a WrappedImage, using an integer array of colour values as
     * source if necessary
     * @param opcode Code defining operation to perform
     * @param biDst Destination image for operation
     * @param dstwidth Width of destination image
     * @param x X-offset of destination area within destination image
     * @param y Y-offset of destination area within destination image
     * @param cx Width of destination area
     * @param cy Height of destination area
     * @param src Source data, represented as an array of integer pixel values
     * @param srcwidth Width of source data
     * @param srcx X-offset of source area within source data
     * @param srcy Y-offset of source area within source data
     */
	public void do_array(int opcode, WrappedImage biDst, int dstwidth, int x, int y,
			int cx, int cy, int[] src, int srcwidth, int srcx, int srcy) {
//		int Bpp = Options.Bpp;
        //int[] dst = null;
		// System.out.println("do_array: opcode = 0x" +
		// Integer.toHexString(opcode) );
		switch (opcode) {
		case 0x0:
			ropClear(biDst, dstwidth, x, y, cx, cy);
			break;
		case 0x1:
			ropNor(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0x2:
			ropAndInverted(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx,
					srcy);
			break;
		case 0x3: // CopyInverted
			ropInvert(biDst, src, srcwidth, srcx, srcy, cx, cy);
			ropCopy(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0x4: // AndReverse
			ropInvert(biDst, null, dstwidth, x, y, cx, cy);
			ropAnd(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0x5:
			ropInvert(biDst, null, dstwidth, x, y, cx, cy);
			break;
		case 0x6:
			ropXor(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0x7:
			ropNand(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0x8:
			ropAnd(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0x9:
			ropEquiv(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0xa: // Noop
			break;
		case 0xb:
			ropOrInverted(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx,
					srcy);
			break;
		case 0xc:
			ropCopy(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0xd: // OrReverse
			ropInvert(biDst, null, dstwidth, x, y, cx, cy);
			ropOr(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0xe:
			ropOr(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy);
			break;
		case 0xf:
			ropSet(biDst, dstwidth, x, y, cx, cy);
			break;
		default:
			logger.warn("do_array unsupported opcode: " + opcode);
		// rop_array(opcode,dst,dstwidth,x,y,cx,cy,src,srcwidth,srcx,srcy);
		}
	}
    
    /**
     * Perform an operation on a single pixel in a WrappedImage
     * @param opcode Opcode defining operation to perform
     * @param dst Image on which to perform the operation
     * @param x X-coordinate of pixel to modify
     * @param y Y-coordinate of pixel to modify
     * @param color Colour to use in operation (unused for some operations)
     */
    public void do_pixel(int opcode, WrappedImage dst, int x, int y, int color) {
        int mask = Options.bpp_mask;
        
        if(dst == null) return;
        
        int c = dst.getRGB(x,y);
        
        switch (opcode) {
        case 0x0: dst.setRGB(x,y,0); break;
        case 0x1: dst.setRGB(x,y,(~(c | color)) & mask); break;
        case 0x2: dst.setRGB(x,y, c & ((~color) & mask)); break;
        case 0x3: dst.setRGB(x,y,(~color) & mask); break;
        case 0x4: dst.setRGB(x,y,(~c & color) * mask); break;
        case 0x5: dst.setRGB(x,y,(~c) & mask);  break;
        case 0x6: dst.setRGB(x,y, c ^ ((color) & mask)); break;
        case 0x7: dst.setRGB(x,y,(~c & color) & mask); break;
        case 0x8: dst.setRGB(x,y, c & ( color & mask )); break;
        case 0x9: dst.setRGB(x,y,c ^ ( ~color & mask) ); break;
        case 0xa: /* Noop */ break;
        case 0xb: dst.setRGB(x,y,c | ( ~color & mask )); break;
        case 0xc: dst.setRGB(x,y,color); break;
        case 0xd: dst.setRGB(x,y,(~c | color) & mask); break;
        case 0xe: dst.setRGB(x,y, c | ( color & mask)); break;
        case 0xf: dst.setRGB(x,y,mask); break;
        default:
            logger.warn("do_byte unsupported opcode: " + opcode);
        }
    }

	private void ropNor(WrappedImage biDst, int dstwidth, int x, int y, int cx, int cy,
			int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0x1
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);

        for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
                biDst.setRGB(x+col,y+row,(~(biDst.getRGB(x+col,y+row) | src[psrc])) & mask);               
			}
			psrc += (srcwidth - cx);
		}
	}

	private void ropAndInverted(WrappedImage biDst, int dstwidth, int x, int y, int cx,
			int cy, int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0x2
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
                int c = biDst.getRGB(x+col,y+row);
                biDst.setRGB(x+col,y+row,c & ((~src[psrc]) & mask));              
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private void ropXor(WrappedImage biDst, int dstwidth, int x, int y, int cx, int cy,
			int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0x6
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
                int c = biDst.getRGB(x+col,y+row);
                biDst.setRGB(x+col,y+row, c ^ ((src[psrc]) & mask));
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private void ropNand(WrappedImage biDst, int dstwidth, int x, int y, int cx, int cy,
			int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0x7
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
			    int c = biDst.getRGB(x+col,y+row);
                biDst.setRGB(x+col,y+row, (~(c & src[psrc])) & mask);
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private void ropAnd(WrappedImage biDst, int dstwidth, int x, int y, int cx, int cy,
			int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0x8
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
                int c = biDst.getRGB(x+col,y+row);
                biDst.setRGB(x+col,y+row, c & ((src[psrc]) & mask));
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private void ropEquiv(WrappedImage biDst, int dstwidth, int x, int y, int cx,
			int cy, int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0x9
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
                int c = biDst.getRGB(x+col,y+row);
                biDst.setRGB(x+col,y+row, c ^ ((~src[psrc]) & mask));
                psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private void ropOrInverted(WrappedImage biDst, int dstwidth, int x, int y, int cx,
			int cy, int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0xb
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
                int c = biDst.getRGB(x+col,y+row);
                biDst.setRGB(x+col,y+row, c | ((~src[psrc]) & mask));
                psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private void ropOr(WrappedImage biDst, int dstwidth, int x, int y, int cx, int cy,
			int[] src, int srcwidth, int srcx, int srcy) {
		// opcode 0xe
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
                int c = biDst.getRGB(x+col,y+row);
                biDst.setRGB(x+col,y+row, c | (src[psrc] & mask));
        		psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}
}
