/*
 * This file is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 */

package com.joa_ebert.apparat.swf.tags;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import com.joa_ebert.apparat.swf.tags.control.DebugID;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.swf.tags.control.EndTag;
import com.joa_ebert.apparat.swf.tags.control.ExportAssetsTag;
import com.joa_ebert.apparat.swf.tags.control.FileAttributesTag;
import com.joa_ebert.apparat.swf.tags.control.FrameLabelTag;
import com.joa_ebert.apparat.swf.tags.control.MetadataTag;
import com.joa_ebert.apparat.swf.tags.control.ProductInfoTag;
import com.joa_ebert.apparat.swf.tags.control.ScriptLimitsTag;
import com.joa_ebert.apparat.swf.tags.control.SetBackgroundColorTag;
import com.joa_ebert.apparat.swf.tags.control.ShowFrameTag;
import com.joa_ebert.apparat.swf.tags.control.SymbolClassTag;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsJPEG2Tag;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsJPEG3Tag;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsJPEG4Tag;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsLossless2Tag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Tags
{
	public static final int End = 0;
	public static final int ShowFrame = 1;
	public static final int DefineShape = 2;
	public static final int PlaceObject = 4;
	public static final int RemoveObject = 5;
	public static final int DefineBits = 6;
	public static final int DefineButton = 7;
	public static final int JPEGTables = 8;
	public static final int SetBackgroundColor = 9;
	public static final int DefineFont = 10;
	public static final int DefineText = 11;
	public static final int DoAction = 12;
	public static final int DefineFontInfo = 13;
	public static final int DefineSound = 14;
	public static final int StartSound = 15;
	public static final int DefineButtonSound = 17;
	public static final int SoundStreamHead = 18;
	public static final int SoundStreamBlock = 19;
	public static final int DefineBitsLossless = 20;
	public static final int DefineBitsJPEG2 = 21;
	public static final int DefineShape2 = 22;
	public static final int DefineButtonCxform = 23;
	public static final int Protect = 24;
	public static final int PlaceObject2 = 26;
	public static final int RemoveObject2 = 28;
	public static final int DefineShape3 = 32;
	public static final int DefineText2 = 33;
	public static final int DefineButton2 = 34;
	public static final int DefineBitsJPEG3 = 35;
	public static final int DefineBitsLossless2 = 36;
	public static final int DefineEditText = 37;
	public static final int DefineSprite = 39;
	public static final int ProductInfo = 41;
	public static final int FrameLabel = 43;
	public static final int SoundStreamHead2 = 45;
	public static final int DefineMorphShape = 46;
	public static final int DefineFont2 = 48;
	public static final int ExportAssets = 56;
	public static final int ImportAssets = 57;
	public static final int EnableDebugger = 58;
	public static final int DoInitAction = 59;
	public static final int DefineVideoStream = 60;
	public static final int VideoFrame = 61;
	public static final int DefineFontInfo2 = 62;
	public static final int DebugID = 63;
	public static final int EnableDebugger2 = 64;
	public static final int ScriptLimits = 65;
	public static final int SetTabIndex = 66;
	public static final int FileAttributes = 69;
	public static final int PlaceObject3 = 70;
	public static final int ImportAssets2 = 71;
	public static final int DefineFontAlignZones = 73;
	public static final int CSMTextSettings = 74;
	public static final int DefineFont3 = 75;
	public static final int SymbolClass = 76;
	public static final int Metadata = 77;
	public static final int DefineScalingGrid = 78;
	public static final int DoABC = 82;
	public static final int DefineShape4 = 83;
	public static final int DefineMorphShape2 = 84;
	public static final int DefineSceneAndFrameLabelData = 86;
	public static final int DefineBinaryData = 87;
	public static final int DefineFontName = 88;
	public static final int StartSound2 = 89;
	public static final int DefineBitsJPEG4 = 90;
	public static final int DefineFont4 = 91;

	private static final SortedMap<Integer, Class<? extends ITag>> classMap = new TreeMap<Integer, Class<? extends ITag>>();
	private static final SortedMap<Integer, String> stringMap = new TreeMap<Integer, String>();
	private static final HashMap<Integer, Boolean> writeLongLengthMap = new HashMap<Integer, Boolean>();

	static
	{
		writeLongLengthMap.put( DefineBits, true );
		writeLongLengthMap.put( DefineBitsJPEG2, true );
		writeLongLengthMap.put( DefineBitsJPEG3, true );
		writeLongLengthMap.put( DefineBitsJPEG4, true );
		writeLongLengthMap.put( DefineBitsLossless, true );
		writeLongLengthMap.put( DefineBitsLossless2, true );

		stringMap.put( End, "End" );
		stringMap.put( ShowFrame, "ShowFrame" );
		stringMap.put( DefineShape, "DefineShape" );
		stringMap.put( PlaceObject, "PlaceObject" );
		stringMap.put( RemoveObject, "RemoveObject" );
		stringMap.put( DefineBits, "DefineBits" );
		stringMap.put( DefineButton, "DefineButton" );
		stringMap.put( JPEGTables, "JPEGTables" );
		stringMap.put( SetBackgroundColor, "SetBackgroundColor" );
		stringMap.put( DefineFont, "DefineFont" );
		stringMap.put( DefineText, "DefineText" );
		stringMap.put( DoAction, "DoAction" );
		stringMap.put( DefineFontInfo, "DefineFontInfo" );
		stringMap.put( DefineSound, "DefineSound" );
		stringMap.put( StartSound, "StartSound" );
		stringMap.put( DefineButtonSound, "DefineButtonSound" );
		stringMap.put( SoundStreamHead, "SoundStreamHead" );
		stringMap.put( SoundStreamBlock, "SoundStreamBlock" );
		stringMap.put( DefineBitsLossless, "DefineBitsLossless" );
		stringMap.put( DefineBitsJPEG2, "DefineBitsJPEG2" );
		stringMap.put( DefineShape2, "DefineShape2" );
		stringMap.put( DefineButtonCxform, "DefineButtonCxform" );
		stringMap.put( Protect, "Protect" );
		stringMap.put( PlaceObject2, "PlaceObject2" );
		stringMap.put( RemoveObject2, "RemoveObject2" );
		stringMap.put( DefineShape3, "DefineShape3" );
		stringMap.put( DefineText2, "DefineText2" );
		stringMap.put( DefineButton2, "DefineButton2" );
		stringMap.put( DefineBitsJPEG3, "DefineBitsJPEG3" );
		stringMap.put( DefineBitsLossless2, "DefineBitsLossless2" );
		stringMap.put( DefineEditText, "DefineEditText" );
		stringMap.put( DefineSprite, "DefineSprite" );
		stringMap.put( ProductInfo, "ProductInfo" );
		stringMap.put( FrameLabel, "FrameLabel" );
		stringMap.put( SoundStreamHead2, "SoundStreamHead2" );
		stringMap.put( DefineMorphShape, "DefineMorphShape" );
		stringMap.put( DefineFont2, "DefineFont2" );
		stringMap.put( ExportAssets, "ExportAssets" );
		stringMap.put( ImportAssets, "ImportAssets" );
		stringMap.put( EnableDebugger, "EnableDebugger" );
		stringMap.put( DoInitAction, "DoInitAction" );
		stringMap.put( DefineVideoStream, "DefineVideoStream" );
		stringMap.put( VideoFrame, "VideoFrame" );
		stringMap.put( DefineFontInfo2, "DefineFontInfo2" );
		stringMap.put( DebugID, "DebugID" );
		stringMap.put( EnableDebugger2, "EnableDebugger2" );
		stringMap.put( ScriptLimits, "ScriptLimits" );
		stringMap.put( SetTabIndex, "SetTabIndex" );
		stringMap.put( FileAttributes, "FileAttributes" );
		stringMap.put( PlaceObject3, "PlaceObject3" );
		stringMap.put( ImportAssets2, "ImportAssets2" );
		stringMap.put( DefineFontAlignZones, "DefineFontAlignZones" );
		stringMap.put( CSMTextSettings, "CSMTextSettings" );
		stringMap.put( DefineFont3, "DefineFont3" );
		stringMap.put( SymbolClass, "SymbolClass" );
		stringMap.put( Metadata, "Metadata" );
		stringMap.put( DefineScalingGrid, "DefineScalingGrid" );
		stringMap.put( DoABC, "DoABC" );
		stringMap.put( DefineShape4, "DefineShape4" );
		stringMap.put( DefineMorphShape2, "DefineMorphShape2" );
		stringMap.put( DefineSceneAndFrameLabelData,
				"DefineSceneAndFrameLabelData" );
		stringMap.put( DefineBinaryData, "DefineBinaryData" );
		stringMap.put( DefineFontName, "DefineFontName" );
		stringMap.put( StartSound2, "StartSound2" );
		stringMap.put( DefineBitsJPEG4, "DefineBitsJPEG4" );
		stringMap.put( DefineFont4, "DefineFont4" );

		classMap.put( DebugID, DebugID.class );
		classMap.put( DoABC, DoABCTag.class );
		classMap.put( End, EndTag.class );
		classMap.put( ExportAssets, ExportAssetsTag.class );
		classMap.put( FileAttributes, FileAttributesTag.class );
		classMap.put( FrameLabel, FrameLabelTag.class );
		classMap.put( Metadata, MetadataTag.class );
		classMap.put( ProductInfo, ProductInfoTag.class );
		classMap.put( ScriptLimits, ScriptLimitsTag.class );
		classMap.put( SetBackgroundColor, SetBackgroundColorTag.class );
		classMap.put( ShowFrame, ShowFrameTag.class );
		classMap.put( SymbolClass, SymbolClassTag.class );

		classMap.put( DefineBitsJPEG2, DefineBitsJPEG2Tag.class );
		classMap.put( DefineBitsJPEG3, DefineBitsJPEG3Tag.class );
		classMap.put( DefineBitsJPEG4, DefineBitsJPEG4Tag.class );
		classMap.put( DefineBitsLossless2, DefineBitsLossless2Tag.class );
	}

	public static ITag createTag( final int type )
			throws InstantiationException, IllegalAccessException
	{
		return getTagClass( type ).newInstance();
	}

	public static Class<? extends ITag> getTagClass( final int type )
	{
		Class<? extends ITag> klass = classMap.get( type );

		if( null == klass )
		{
			klass = GenericTag.class;
		}

		return klass;
	}

	public static boolean isLongLength( final int type )
	{
		if( writeLongLengthMap.containsKey( type ) )
		{
			return true;
		}
		return false;
	}

	public static String typeToString( final int type )
	{
		String name = stringMap.get( type );

		if( null == name )
		{
			name = "(Unknown " + Integer.toString( type ) + ")";
		}

		return name;
	}

	private Tags()
	{
	}
}
