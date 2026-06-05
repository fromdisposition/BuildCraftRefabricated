/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

@Deprecated

public interface IPipeConnection {

    enum ConnectOverride {

        CONNECT,
        DISCONNECT,
        DEFAULT
    }

    ConnectOverride overridePipeConnection(Object type, Direction with);
}
