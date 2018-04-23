package io.dico.dicore;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;

public class RegistratorTest {

    private static class ListenerFieldClass {

        private interface EntityDamageByEntityEventListener extends Registrator.IEventListener<EntityDamageByEntityEvent> {

        }

        @Registrator.ListenerInfo
        private final EntityDamageByEntityEventListener onEntityDamageByEntity = (event) -> {

        };

        @Registrator.ListenerInfo
        private final Registrator.IEventListener<PlayerJoinEvent> onPlayerJoin = (event) -> {

        };

        @Registrator.ListenerInfo(events = {"player.PlayerQuitEvent", "player.PlayerKickEvent", "player.PlayerChangedWorldEvent"})
        private final Registrator.IEventListener<PlayerEvent> onPlayerQuit = (event) -> {

        };

    }

    @Test
    public void testRegistrator_registerListenerFields() {
        Registrator registrator = new Registrator();
        registrator.registerListeners(new ListenerFieldClass());
        Assert.assertEquals(5, registrator.getListeners().size());
    }

    private static class ListenerFieldClassWithError1 {

        @Registrator.ListenerInfo(events = {"player.PlayerQuitEvent", "player.PlayerKickEvent", "player.PlayerChangedWorldEvent"})
        private final Registrator.IEventListener<EntityEvent> onPlayerQuit = (event) -> {

        };

    }

    @Test(expected = Registrator.ListenerFieldError.class)
    public void testRegistrator_registerListenerFields_ExpectError1() {
        Registrator registrator = new Registrator();
        registrator.registerListeners(new ListenerFieldClassWithError1());
    }

    private static class ListenerFieldClassWithError2 {

        @Registrator.ListenerInfo
        private final Consumer<PlayerQuitEvent> onPlayerQuit = (event) -> {

        };

    }

    @Test(expected = Registrator.ListenerFieldError.class)
    public void testRegistrator_registerListenerFields_ExpectError2() {
        Registrator registrator = new Registrator();
        registrator.registerListeners(new ListenerFieldClassWithError2());
    }

}
