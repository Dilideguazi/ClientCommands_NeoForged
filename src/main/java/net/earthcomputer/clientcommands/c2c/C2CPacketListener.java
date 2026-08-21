package net.earthcomputer.clientcommands.c2c;

import net.earthcomputer.clientcommands.c2c.packets.*;
import net.minecraft.network.ClientboundPacketListener;

public interface C2CPacketListener extends ClientboundPacketListener {
    void onMessageC2CPacket(MessageC2CPacket packet);

    void onStartTwoPlayerGameC2CPacket(StartTwoPlayerGameC2CPacket packet);

    void onStopTwoPlayerGameC2CPacket(StopTwoPlayerGameC2CPacket packet);

    void onPutTicTacToeMarkC2CPacket(PutTicTacToeMarkC2CPacket packet);

    void onPutConnectFourPieceC2CPacket(PutConnectFourPieceC2CPacket packet);

    void onChessResignPacket(ChessResignC2CPacket packet);

    void onChessMovePacket(ChessMoveC2CPacket packet);

    void onChessDrawOfferPacket(ChessDrawOfferC2CPacket packet);
}
