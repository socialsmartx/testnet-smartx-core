package com.smartx.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.smartx.block.Block;

public class MCollection {
    public Block mblock = null;
    public List<Block> blocks = Collections.synchronizedList(new ArrayList<Block>());
}
