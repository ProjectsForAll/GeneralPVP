package host.plas.generalpvp.commands;

import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.config.bits.ConfiguredEnchantment;
import host.plas.generalpvp.config.bits.ConfiguredItem;
import host.plas.generalpvp.config.bits.ConfiguredPotion;
import org.bukkit.Material;

import java.util.concurrent.ConcurrentSkipListSet;

public class ManageCMD extends SimplifiedCommand {
    public ManageCMD() {
        super("gpvpmanage", GeneralPVP.getInstance());
    }

    @Override
    public boolean command(CommandContext ctx) {
        if (! ctx.isArgUsable(0)) {
            return sendUsage(ctx);
        }

        String sub = ctx.getStringArg(0).toLowerCase();
        switch (sub) {
            case "pvp":
                return handlePVP(ctx);
            case "items":
                return handleItems(ctx);
            case "potions":
                return handlePotions(ctx);
            case "enchantments":
                return handleEnchantments(ctx);
            case "cooldowns":
                return handleCooldowns(ctx);
            default:
                return sendUsage(ctx);
        }
    }

    public boolean sendUsage(CommandContext ctx) {
        ctx.sendMessage("&cUsage Key&8:");
        ctx.sendMessage("&e<> &7- &dRequired Argument &9| &e() &7- &dOptional Argument &9| &e| &7- &dOptions Separator");
        ctx.sendMessage("&cUsage&8:");
        ctx.sendMessage("&2/&agpvpmanage &b<pvp> &d<cpvp|bpvp|apvp> &c<true|false>");
        ctx.sendMessage("&2/&agpvpmanage &b<items> &d<drop-excess> &c<true|false>");
        ctx.sendMessage("&2/&agpvpmanage &b<items> &d<add> &c<identifier> &a<material> &e<amount>");
        ctx.sendMessage("&2/&agpvpmanage &b<items> &d<remove> &c<identifier>");
        ctx.sendMessage("&2/&agpvpmanage &b<items> &d<list>");
        ctx.sendMessage("&2/&agpvpmanage &b<potions> &d<add> &c<identifier> &a<potion> &e<amplifier>");
        ctx.sendMessage("&2/&agpvpmanage &b<potions> &d<remove> &c<identifier>");
        ctx.sendMessage("&2/&agpvpmanage &b<potions> &d<list>");
        ctx.sendMessage("&2/&agpvpmanage &b<enchantments> &d<add> &c<identifier> &a<enchantment> &e<amplifier>");
        ctx.sendMessage("&2/&agpvpmanage &b<enchantments> &d<remove> &c<identifier>");
        ctx.sendMessage("&2/&agpvpmanage &b<enchantments> &d<list>");
        ctx.sendMessage("&2/&agpvpmanage &b<cooldowns> &d<pearls> &c(ticks)");
        return false;
    }

    private boolean handlePVP(CommandContext ctx) {
        if (! ctx.isArgUsable(2)) return sendUsage(ctx);
        String type = ctx.getStringArg(1).toLowerCase();
        boolean allow = Boolean.parseBoolean(ctx.getStringArg(2));

        switch (type) {
            case "cpvp":
                return setAllowCPVP(ctx, allow);
            case "bpvp":
                return setAllowBPVP(ctx, allow);
            case "apvp":
                return setAllowAPVP(ctx, allow);
            default:
                return sendUsage(ctx);
        }
    }

    private boolean handleItems(CommandContext ctx) {
        if (! ctx.isArgUsable(1)) return sendUsage(ctx);
        String sub = ctx.getStringArg(1).toLowerCase();

        switch (sub) {
            case "drop-excess":
                if (! ctx.isArgUsable(2)) return sendUsage(ctx);
                boolean drop = Boolean.parseBoolean(ctx.getStringArg(2));
                GeneralPVP.getMainConfig().setDropExcess(drop);
                ctx.sendMessage("&eSet &bDrop Excess &eto &b" + drop + "&7.");
                return true;
            case "add":
                if (! ctx.isArgUsable(4)) return sendUsage(ctx);
                String identifier = ctx.getStringArg(2);
                String materialStr = ctx.getStringArg(3).toUpperCase();
                int amount = 64;
                try {
                    amount = Integer.parseInt(ctx.getStringArg(4));
                } catch (Exception e) {}
                try {
                    Material material = Material.valueOf(materialStr);
                    ConfiguredItem item = new ConfiguredItem(identifier, material, amount);
                    GeneralPVP.getMainConfig().addItemConfiguration(item);
                    ctx.sendMessage("&eAdded item configuration &b" + identifier + " &7(&a" + material.name() + "&7, &e" + amount + "&7).");
                    return true;
                } catch (IllegalArgumentException e) {
                    ctx.sendMessage("&cInvalid material: " + materialStr);
                    return false;
                }
            case "remove":
                if (! ctx.isArgUsable(2)) return sendUsage(ctx);
                String toRemove = ctx.getStringArg(2);
                GeneralPVP.getMainConfig().removeItemConfiguration(toRemove);
                ctx.sendMessage("&eRemoved item configuration &b" + toRemove + "&7.");
                return true;
            case "list":
                ctx.sendMessage("&eItem Configurations&8:");
                GeneralPVP.getMainConfig().getItemConfigurations().forEach(item ->
                        ctx.sendMessage("&7- &b" + item.getIdentifier() + "&8: &a" + item.getMaterial().name() + " &7(Max: &e" + item.getMaxAmount() + "&7)"));
                return true;
            default:
                return sendUsage(ctx);
        }
    }

    private boolean handlePotions(CommandContext ctx) {
        if (! ctx.isArgUsable(1)) return sendUsage(ctx);
        String sub = ctx.getStringArg(1).toLowerCase();

        switch (sub) {
            case "add":
                if (! ctx.isArgUsable(4)) return sendUsage(ctx);
                String identifier = ctx.getStringArg(2);
                String potionType = ctx.getStringArg(3);
                int amplifier = 1;
                try {
                    amplifier = Integer.parseInt(ctx.getStringArg(4));
                } catch (Exception e) {}
                ConfiguredPotion potion = new ConfiguredPotion(identifier, potionType, amplifier);
                GeneralPVP.getMainConfig().addPotionConfiguration(potion);
                ctx.sendMessage("&eAdded potion configuration &b" + identifier + " &7(&a" + potionType + "&7, &e" + amplifier + "&7).");
                return true;
            case "remove":
                if (! ctx.isArgUsable(2)) return sendUsage(ctx);
                String toRemove = ctx.getStringArg(2);
                GeneralPVP.getMainConfig().removePotionConfiguration(toRemove);
                ctx.sendMessage("&eRemoved potion configuration &b" + toRemove + "&7.");
                return true;
            case "list":
                ctx.sendMessage("&ePotion Configurations&8:");
                GeneralPVP.getMainConfig().getPotionConfigurations().forEach(p ->
                        ctx.sendMessage("&7- &b" + p.getIdentifier() + "&8: &a" + p.getType() + " &7(Amplifier: &e" + p.getAmplifier() + "&7)"));
                return true;
            default:
                return sendUsage(ctx);
        }
    }

    private boolean handleEnchantments(CommandContext ctx) {
        if (! ctx.isArgUsable(1)) return sendUsage(ctx);
        String sub = ctx.getStringArg(1).toLowerCase();

        switch (sub) {
            case "add":
                if (! ctx.isArgUsable(4)) return sendUsage(ctx);
                String identifier = ctx.getStringArg(2);
                String enchType = ctx.getStringArg(3);
                int amp = 1;
                try {
                    amp = Integer.parseInt(ctx.getStringArg(4));
                } catch (Exception e) {}
                ConfiguredEnchantment enchantment = new ConfiguredEnchantment(identifier, enchType, amp);
                GeneralPVP.getMainConfig().addEnchantmentConfiguration(enchantment);
                ctx.sendMessage("&eAdded enchantment configuration &b" + identifier + " &7(&a" + enchType + "&7, &e" + amp + "&7).");
                return true;
            case "remove":
                if (! ctx.isArgUsable(2)) return sendUsage(ctx);
                String toRemove = ctx.getStringArg(2);
                GeneralPVP.getMainConfig().removeEnchantmentConfiguration(toRemove);
                ctx.sendMessage("&eRemoved enchantment configuration &b" + toRemove + "&7.");
                return true;
            case "list":
                ctx.sendMessage("&eEnchantment Configurations&8:");
                GeneralPVP.getMainConfig().getEnchantmentConfigurations().forEach(e ->
                        ctx.sendMessage("&7- &b" + e.getIdentifier() + "&8: &a" + e.getType() + " &7(Amplifier: &e" + e.getAmplifier() + "&7)"));
                return true;
            default:
                return sendUsage(ctx);
        }
    }

    private boolean handleCooldowns(CommandContext ctx) {
        if (! ctx.isArgUsable(1)) return sendUsage(ctx);
        String sub = ctx.getStringArg(1).toLowerCase();

        if (sub.equals("pearls")) {
            long ticks = GeneralPVP.getMainConfig().getPearlCooldown();
            if (ctx.isArgUsable(2)) {
                try {
                    ticks = Long.parseLong(ctx.getStringArg(2));
                    GeneralPVP.getMainConfig().setPearlCooldown(ticks);
                    ctx.sendMessage("&eSet &bPearl Cooldown &eto &b" + ticks + " &eticks&7.");
                } catch (Exception e) {
                    ctx.sendMessage("&cInvalid ticks: " + ctx.getStringArg(2));
                }
            } else {
                ctx.sendMessage("&eCurrent &bPearl Cooldown&8: &b" + ticks + " &eticks&7.");
            }
            return true;
        }

        return sendUsage(ctx);
    }

    public boolean setAllowCPVP(CommandContext ctx, boolean allow) {
        GeneralPVP.getMainConfig().setAllowCrystalPVP(allow);
        ctx.sendMessage("&eSet &bCrystal PvP &eto &b" + allow + "&7.");

        return true;
    }

    public boolean setAllowBPVP(CommandContext ctx, boolean allow) {
        GeneralPVP.getMainConfig().setAllowBedPVP(allow);
        ctx.sendMessage("&eSet &bBed PvP &eto &b" + allow + "&7.");

        return true;
    }

    public boolean setAllowAPVP(CommandContext ctx, boolean allow) {
        GeneralPVP.getMainConfig().setAllowAnchorPVP(allow);
        ctx.sendMessage("&eSet &bAnchor PvP &eto &b" + allow + "&7.");

        return true;
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet<>();

        if (ctx.getArgCount() <= 1) {
            completions.add("pvp");
            completions.add("items");
            completions.add("potions");
            completions.add("enchantments");
            completions.add("cooldowns");
        } else if (ctx.getArgCount() == 2) {
            String firstArg = ctx.getStringArg(0).toLowerCase();
            switch (firstArg) {
                case "pvp":
                    completions.add("cpvp");
                    completions.add("bpvp");
                    completions.add("apvp");
                    break;
                case "items":
                    completions.add("drop-excess");
                    completions.add("add");
                    completions.add("remove");
                    completions.add("list");
                    break;
                case "potions":
                    completions.add("add");
                    completions.add("remove");
                    completions.add("list");
                    break;
                case "enchantments":
                    completions.add("add");
                    completions.add("remove");
                    completions.add("list");
                    break;
                case "cooldowns":
                    completions.add("pearls");
                    break;
            }
        } else if (ctx.getArgCount() == 3) {
            String firstArg = ctx.getStringArg(0).toLowerCase();
            String secondArg = ctx.getStringArg(1).toLowerCase();
            if (firstArg.equals("pvp")) {
                if (secondArg.equals("cpvp") || secondArg.equals("bpvp") || secondArg.equals("apvp")) {
                    completions.add("true");
                    completions.add("false");
                }
            } else if (firstArg.equals("items")) {
                if (secondArg.equals("remove")) {
                    GeneralPVP.getMainConfig().getItemConfigurations().forEach(item ->
                            completions.add(item.getIdentifier()));
                }
            } else if (firstArg.equals("potions")) {
                if (secondArg.equals("remove")) {
                    GeneralPVP.getMainConfig().getPotionConfigurations().forEach(potion ->
                            completions.add(potion.getIdentifier()));
                }
            } else if (firstArg.equals("enchantments")) {
                if (secondArg.equals("remove")) {
                    GeneralPVP.getMainConfig().getEnchantmentConfigurations().forEach(enchantment ->
                            completions.add(enchantment.getIdentifier()));
                }
            }
        }
        return completions;
    }
}
