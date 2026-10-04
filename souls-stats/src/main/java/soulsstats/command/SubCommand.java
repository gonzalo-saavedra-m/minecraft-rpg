package soulsstats.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandSourceStack;

/**
 * Un subcomando de /soulsstats: su nombre, qué hace (lo lista help; en inglés, se traduce con la clave
 * soulsstats.command.<nombre>) y cómo se arma. build recibe el literal ya creado con el nombre y le agrega permisos,
 * argumentos y qué ejecuta; corre al iniciar el servidor.
 */
record SubCommand(String name, String description, UnaryOperator<LiteralArgumentBuilder<CommandSourceStack>> build) {
}
