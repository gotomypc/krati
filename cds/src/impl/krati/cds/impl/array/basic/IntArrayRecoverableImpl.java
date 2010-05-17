package krati.cds.impl.array.basic;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import krati.cds.array.IntArray;
import krati.cds.impl.array.entry.EntryIntFactory;
import krati.cds.impl.array.entry.EntryValueInt;

public class IntArrayRecoverableImpl extends RecoverableArrayImpl<int[], EntryValueInt> implements IntArray
{
  private static final Logger _log = Logger.getLogger(IntArrayRecoverableImpl.class);
  
  public IntArrayRecoverableImpl(Config config) throws Exception
  {
    this(config.getMemberIdStart(),
         config.getMemberIdCount(),
         config.getMaxEntrySize(),
         config.getMaxEntries(),
         config.getCacheDirectory());
  }
  
  public IntArrayRecoverableImpl(int memberIdStart,
                                 int memberIdCount,
                                 int maxEntrySize,
                                 int maxEntries,
                                 File cacheDirectory) throws Exception
  {
    super(memberIdStart, memberIdCount, 4 /* elementSize */, maxEntrySize, maxEntries, cacheDirectory, new EntryIntFactory());
  }
  
  @Override
  protected void loadArrayFileData()
  {
    long maxScn = 0;
    
    try
    {
      maxScn = _arrayFile.readMaxSCN();
      _internalArray = _arrayFile.loadIntArray();
      if (_internalArray.length != _memberIdCount)
      {
        maxScn = 0;
        _internalArray = new int[_memberIdCount];
        clear();
        
        _log.warn("Allocated _internalArray due to invalid length");
      }
      else
      {
        _log.info("Data loaded successfully from file " + _arrayFile.getName());
      }
    }
    catch(Exception e)
    {
      maxScn = 0;
      _internalArray = new int[_memberIdCount];
      clear();
      
      _log.warn("Allocated _internalArray due to a thrown exception: " + e.getMessage());
    }
    
    _entryManager.setWaterMarks(maxScn, maxScn);
  }
  
  /**
   * Sync-up the high water mark to a given value.
   * 
   * @param endOfPeriod
   */
  @Override
  public void saveHWMark(long endOfPeriod)
  {
    if (getHWMark() < endOfPeriod)
    {
      try
      {
        setData(getIndexStart(), getData(getIndexStart()), endOfPeriod);
      }
      catch(Exception e)
      {
        _log.error(e);
      }
    }
  }

  @Override
  public void clear()
  {
    if (_internalArray != null)
    {
      for (int i = 0; i < _internalArray.length; i ++)
      {
        _internalArray[i] = 0;
      }
    }

    // Clear the entry manager
    _entryManager.clear();
    
    // Clear the underly array file
    try
    {
      _arrayFile.reset(_internalArray, _entryManager.getLWMark());
    }
    catch(IOException e)
    {
      _log.error(e.getMessage(), e);
    }
  }
  
  public int getData(int index)
  {
    return _internalArray[index - _memberIdStart];
  }
  
  public void setData(int index, int value, long scn) throws Exception
  {
    int pos = index - _memberIdStart;
    _internalArray[pos] = value;
    _entryManager.addToEntry(new EntryValueInt(pos, value, scn));  
  }
  
  @Override
  public Object memoryClone()
  {
      IntArrayMemoryImpl memClone = new IntArrayMemoryImpl(getIndexStart(), length());
      
      System.arraycopy(_internalArray, 0, memClone.getInternalArray(), 0, _internalArray.length);
      memClone._lwmScn = getLWMark(); 
      memClone._hwmScn = getHWMark();
      
      return memClone;
  }
  
  public static class Config extends RecoverableArrayImpl.Config<EntryValueInt>
  {
    // super configuration class provides everything
  }
}