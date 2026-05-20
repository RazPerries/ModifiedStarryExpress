package org.aussiebox.starexpress.client.gui.screen;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.config.StarryExpressServerConfig;
import org.aussiebox.starexpress.util.RoleInfo;
import org.aussiebox.starexpress.util.RoleInfo.GuidebookEntry;
import org.aussiebox.starexpress.util.RoleInfo.RoleType;
import org.jetbrains.annotations.NotNull;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class GuidebookScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    public Map<String, RoleInfo> roleInfo = new TreeMap<>();
    public Map<String, RoleInfo> getRoleInfo() {

        for (Role role : WatheRoles.ROLES) {
            if (!Language.getInstance().has("guidebook.role." + role.identifier())) continue;
            GuidebookEntry entry = GuidebookEntry.NONE;
            if (role.isInnocent() && !role.canUseKiller()) entry = GuidebookEntry.GOOD;
            if (!role.isInnocent() && !role.canUseKiller()) entry = GuidebookEntry.NEUTRAL;
            if (!role.isInnocent() && role.canUseKiller()) entry = GuidebookEntry.EVIL;
            roleInfo.putIfAbsent(String.valueOf(role.identifier()), new RoleInfo(role.identifier().getNamespace(), RoleType.ROLE, role.color(), entry));
        }

        for (Modifier mod : HMLModifiers.MODIFIERS) {
            if (!Language.getInstance().has("guidebook.role." + mod.identifier())) continue;
            roleInfo.putIfAbsent(String.valueOf(mod.identifier()), new RoleInfo(mod.identifier().getNamespace(), RoleType.MODIFIER, mod.color(), GuidebookEntry.NONE));
        }

        return roleInfo;
    }

    public Map<String, String> roleCreators = new HashMap<>();
    public Map<String, String> getRoleCreators() {

        if (FabricLoader.getInstance().isModLoaded("stupid_express")) {
            roleCreators.putIfAbsent(String.valueOf(SERoles.AVARICIOUS.identifier()), "Sonicdude");
        }

        return roleCreators;
    }

    public Map<String, ButtonComponent> roleButtons = new TreeMap<>();

    private String displayedEntry;
    private ButtonComponent displayedEntryButton;

    private final ScrollContainer<FlowLayout> roleButtonList = Containers.verticalScroll(Sizing.expand(40), Sizing.expand(), Containers.verticalFlow(Sizing.content(), Sizing.content())).scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE)).scrollbarThiccness(1).scrollStep(12);
    private ScrollContainer<FlowLayout> currentRoleButtonList;

    private final ScrollContainer<FlowLayout> quickTravelList = Containers.verticalScroll(Sizing.expand(40), Sizing.expand(10), Containers.verticalFlow(Sizing.content(), Sizing.content())).scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE)).scrollbarThiccness(1).scrollStep(12);
    private ScrollContainer<FlowLayout> currentQuickTravelList;

    private final FlowLayout informationFlow = Containers.verticalFlow(Sizing.expand(), Sizing.fill());
    private FlowLayout currentInformationFlow;

    @Override
    protected void build(FlowLayout root) {
        getRoleInfo();
        getRoleCreators();

        setRoleButtonList(roleButtonList);
        setInformationFlow(informationFlow);

        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.child(Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(getRoleButtonList().id("role_button_list"))
                .padding(Insets.of(10))
                .id("role_buttons"));
        root.child(getInformationFlow().padding(Insets.of(10)));

        /// OPEN CURRENT ROLE IF POSSIBLE

        Player player = Minecraft.getInstance().player;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());

        if (!game.isRunning()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        String roleID = game.getRole(player).identifier().toString();
        if (!roleInfo.containsKey(roleID)) return;

        setQuickTravelList(quickTravelList);
        root.childById(FlowLayout.class, "role_buttons")
                .child(Components.box(Sizing.fill(38), Sizing.fixed(1)).color(Color.ofArgb(0x33FFFFFF)).margins(Insets.of(4, 0, 10, 10)))
                .child(getQuickTravelList());
        this.currentRoleButtonList.verticalSizing(Sizing.expand(90));

        openToEntry(roleID);
    }

    public FlowLayout getInformationFlow() {
        return this.currentInformationFlow;
    }

    public void setInformationFlow(FlowLayout layout) {
        this.currentInformationFlow = layout;
        this.currentInformationFlow.clearChildren();

        ScrollContainer<FlowLayout> roleDescription = Containers.verticalScroll(Sizing.fill(), Sizing.fill(), Containers.verticalFlow(Sizing.content(), Sizing.content())).scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE)).scrollbarThiccness(1).scrollStep(8);
        FlowLayout scrollLayout = (FlowLayout) roleDescription.children().getFirst();
        scrollLayout.clearChildren();
        scrollLayout.child(Components.label(Component.empty()).horizontalTextAlignment(HorizontalAlignment.LEFT).sizing(Sizing.fill(), Sizing.content()).id("role_description"));
        scrollLayout.child(Components.box(Sizing.fill(), Sizing.fixed(80)).color(Color.ofArgb(0x00000000)));

        this.currentInformationFlow.child(Components.label(Component.empty().withStyle(Style.EMPTY.withFont(StarryExpress.id("guidebook_heading")))).lineHeight(18).horizontalTextAlignment(HorizontalAlignment.LEFT).sizing(Sizing.fill(), Sizing.content()).margins(Insets.bottom(3)).id("role_name")).padding(Insets.horizontal(10));
        this.currentInformationFlow.child(Components.label(Component.empty()).horizontalTextAlignment(HorizontalAlignment.LEFT).sizing(Sizing.fill(), Sizing.content()).margins(Insets.bottom(3)).id("role_title")).padding(Insets.horizontal(10));
        this.currentInformationFlow.child(Components.label(Component.empty()).horizontalTextAlignment(HorizontalAlignment.LEFT).sizing(Sizing.fill(), Sizing.content()).margins(Insets.bottom(10)).id("role_credits")).padding(Insets.horizontal(10));
        this.currentInformationFlow.child(roleDescription).padding(Insets.horizontal(10)).id("role_description_container");
    }

    public ScrollContainer<FlowLayout> getRoleButtonList() {
        return this.currentRoleButtonList;
    }

    public void setRoleButtonList(ScrollContainer<FlowLayout> container) {
        this.currentRoleButtonList = container;
        FlowLayout layout = (FlowLayout) this.currentRoleButtonList.children().getFirst();
        layout.clearChildren();
        roleButtons.clear();

        CollapsibleContainer rolesContainer = Containers.collapsible(Sizing.fill(), Sizing.content(), Component.translatable("guidebook.category.roles"), true);
            CollapsibleContainer goodRolesContainer = Containers.collapsible(Sizing.fill(), Sizing.content(), Component.translatable("guidebook.category.roles.good"), false);
            CollapsibleContainer neutralRolesContainer = Containers.collapsible(Sizing.fill(), Sizing.content(), Component.translatable("guidebook.category.roles.neutral"), false);
            CollapsibleContainer evilRolesContainer = Containers.collapsible(Sizing.fill(), Sizing.content(), Component.translatable("guidebook.category.roles.evil"), false);
        CollapsibleContainer modifiersContainer = Containers.collapsible(Sizing.fill(), Sizing.content(), Component.translatable("guidebook.category.modifiers"), false);

        for (String roleID : roleInfo.keySet()) {
            RoleInfo roleData = roleInfo.get(roleID);

            ButtonComponent button = Components.button(
                            Component.translatable("guidebook.role." + roleID).withColor(0xFFFFFF).append("                                                                                         "),
                            buttonComponent -> setDisplayedEntry(roleID)
            ).renderer(ButtonComponent.Renderer.texture(StarryExpress.id("textures/empty.png"), 0, 0, 1, 1));

            if (roleData.type() == RoleType.ROLE) {
                if (roleData.guidebookEntry() == GuidebookEntry.GOOD) {
                    goodRolesContainer.child(button.sizing(Sizing.content(), Sizing.content()).id(roleID));
                }
                if (roleData.guidebookEntry() == GuidebookEntry.NEUTRAL) {
                    neutralRolesContainer.child(button.sizing(Sizing.content(), Sizing.content()).id(roleID));
                }
                if (roleData.guidebookEntry() == GuidebookEntry.EVIL) {
                    evilRolesContainer.child(button.sizing(Sizing.content(), Sizing.content()).id(roleID));
                }
            } else if (roleData.type() == RoleType.MODIFIER) {
                modifiersContainer.child(button.sizing(Sizing.content(), Sizing.content()).id(roleID));
            }

            roleButtons.putIfAbsent(roleID, button);
        }

        FlowLayout rolesLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        rolesLayout.child(goodRolesContainer.id("category.roles.good"));
        rolesLayout.child(neutralRolesContainer.id("category.roles.neutral"));
        rolesLayout.child(evilRolesContainer.id("category.roles.evil"));

        rolesContainer.child(rolesLayout.id("category.roles-layout"));

        layout.child(rolesContainer.id("category.roles"));
        layout.child(modifiersContainer.id("category.modifiers"));
        layout.child(Components.box(Sizing.fill(), Sizing.fixed(20)).color(Color.ofArgb(0x00000000)));
    }

    public ScrollContainer<FlowLayout> getQuickTravelList() {
        return this.currentQuickTravelList;
    }

    public void setQuickTravelList(ScrollContainer<FlowLayout> container) {
        this.currentQuickTravelList = container;
        FlowLayout layout = (FlowLayout) this.currentQuickTravelList.children().getFirst();
        layout.clearChildren();

        Player player = Minecraft.getInstance().player;
        GameWorldComponent world = GameWorldComponent.KEY.get(player.level());
        WorldModifierComponent mod = WorldModifierComponent.KEY.get(player.level());

        CollapsibleContainer modifierContainer = Containers.collapsible(Sizing.expand(40), Sizing.content(), Component.translatable("guidebook.category.modifiers"), true);

        for (Modifier modifier : mod.getModifiers(player)) {
            String modID = modifier.identifier.toString();
            if (!roleInfo.containsKey(modID)) continue;

            ButtonComponent button = Components.button(
                    Component.translatable("guidebook.role." + modID).withColor(0xFFFFFF).append("                                                                                         "),
                    buttonComponent -> openToEntry(modID)
            ).renderer(ButtonComponent.Renderer.texture(StarryExpress.id("textures/empty.png"), 0, 0, 1, 1));

            modifierContainer.child(button.sizing(Sizing.content(), Sizing.content()).id("quick_travel." + modID));
        }

        String roleID = world.getRole(player).identifier().toString();
        ButtonComponent button = Components.button(
                Component.translatable("guidebook.role." + roleID).withColor(0xFFFFFF).append("                                                                                         "),
                buttonComponent -> openToEntry(roleID)
        ).renderer(ButtonComponent.Renderer.texture(StarryExpress.id("textures/empty.png"), 0, 0, 1, 1));

        CollapsibleContainer quickTravelContainer = Containers.collapsible(Sizing.expand(), Sizing.content(), Component.translatable("guidebook.category.quick_travel"), false);

        if (roleInfo.containsKey(roleID)) quickTravelContainer.child(button.sizing(Sizing.content(), Sizing.content()).id("quick_travel." + roleID));
        quickTravelContainer.child(modifierContainer);
        quickTravelContainer.id("quick_travel_container");

        this.currentQuickTravelList.child(quickTravelContainer);
    }

    public void setDisplayedEntry(String roleID) {
        this.displayedEntry = roleID;
        if (this.displayedEntryButton != null) {
            ButtonComponent oldButton = this.displayedEntryButton;
            oldButton.setMessage(oldButton.getMessage().copy().withColor(0xFFFFFF));
        }

        ButtonComponent newButton = roleButtons.get(roleID);
        newButton.setMessage(newButton.getMessage().copy().withColor(roleInfo.get(roleID).color()));

        LabelComponent roleName = this.currentInformationFlow.childById(LabelComponent.class, "role_name");
        LabelComponent roleTitle = this.currentInformationFlow.childById(LabelComponent.class, "role_title");
        LabelComponent roleCredits = this.currentInformationFlow.childById(LabelComponent.class, "role_credits");
        LabelComponent roleDescription = this.currentInformationFlow.childById(LabelComponent.class, "role_description");
        roleName.text(Component.translatable("guidebook.role." + roleID).withColor(roleInfo.get(roleID).color()).withStyle(Style.EMPTY.withFont(StarryExpress.id("guidebook_heading"))));
        roleTitle.text(Component.literal("- ").append(Component.translatable("guidebook.role.title." + roleID)).append(" -").withColor(roleInfo.get(roleID).color()));
        roleCredits.text(Component.translatable("guidebook.role.credits").append(Component.translatable("guidebook.namespace." + roleInfo.get(roleID).namespace()).withStyle(Style.EMPTY.withItalic(true))).withColor(0xAAAAAA));
        if (roleCreators.containsKey(roleID)) {
            roleCredits.text(roleCredits.text().copy().append(Component.literal(" (")).withStyle(Style.EMPTY.withItalic(false)).append(Component.translatable("guidebook.role.creator")).append(roleCreators.get(roleID)).append(")"));
        }

        StarryExpressServerConfig.AllergicConfig_ allergicConfig = StarryExpress.CONFIG.allergicConfig;
        roleDescription.text(Component.translatable(
                "guidebook.role.description." + roleID,

                Component.translatable("guidebook.parameter.setting"),
                StarryExpress.CONFIG.starstruckConfig.abilityCooldown(),
                StarryExpress.CONFIG.starstruckConfig.abilityDuration(),
                allergicConfig.nothingChance() + allergicConfig.instinctChance() + allergicConfig.armorChance() + allergicConfig.poisonChance(),
                allergicConfig.nothingChance(),
                allergicConfig.instinctChance(),
                allergicConfig.armorChance(),
                allergicConfig.poisonChance()
        ));
        this.displayedEntryButton = newButton;
    }

    public void openToEntry(String roleID) {
        setDisplayedEntry(roleID);

        CollapsibleContainer roleContainer = this.currentRoleButtonList.childById(CollapsibleContainer.class, "category.roles");
        CollapsibleContainer goodRoleContainer = this.currentRoleButtonList.childById(CollapsibleContainer.class, "category.roles.good");
        CollapsibleContainer neutralRoleContainer = this.currentRoleButtonList.childById(CollapsibleContainer.class, "category.roles.neutral");
        CollapsibleContainer evilRoleContainer = this.currentRoleButtonList.childById(CollapsibleContainer.class, "category.roles.evil");
        CollapsibleContainer modifierContainer = this.currentRoleButtonList.childById(CollapsibleContainer.class, "category.modifiers");

        if (roleInfo.get(roleID).guidebookEntry() == GuidebookEntry.GOOD) {
            if (!roleContainer.expanded()) roleContainer.toggleExpansion();
            if (!goodRoleContainer.expanded()) goodRoleContainer.toggleExpansion();
        }
        if (roleInfo.get(roleID).guidebookEntry() == GuidebookEntry.NEUTRAL) {
            if (!roleContainer.expanded()) roleContainer.toggleExpansion();
            if (!neutralRoleContainer.expanded()) neutralRoleContainer.toggleExpansion();
        }
        if (roleInfo.get(roleID).guidebookEntry() == GuidebookEntry.EVIL) {
            if (!roleContainer.expanded()) roleContainer.toggleExpansion();
            if (!evilRoleContainer.expanded()) evilRoleContainer.toggleExpansion();
        }
        if (roleInfo.get(roleID).type() == RoleType.MODIFIER) {
            if (!modifierContainer.expanded()) modifierContainer.toggleExpansion();
        }

        this.currentRoleButtonList.scrollTo(roleButtons.get(roleID));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (this.currentQuickTravelList == null) return;

        CollapsibleContainer quickTravelContainer = this.currentQuickTravelList.childById(CollapsibleContainer.class, "quick_travel_container");

        Player player = Minecraft.getInstance().player;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (!game.isRunning()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;

        if (quickTravelContainer.expanded()) {
            this.currentQuickTravelList.verticalSizing(Sizing.expand(30));
            this.currentRoleButtonList.verticalSizing(Sizing.expand(70));
        } else {
            this.currentQuickTravelList.verticalSizing(Sizing.expand(10));
            this.currentRoleButtonList.verticalSizing(Sizing.expand(90));
        }
    }


}