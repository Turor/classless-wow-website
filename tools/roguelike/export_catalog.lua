-- Export the Roguelike Gauntlet Boons & Curses catalog (and the dungeon list) to JSON for the website.
--
--   lua5.1 tools/roguelike/export_catalog.lua <mod-turoran-roguelike checkout> [commit] > catalog.json
--
-- Loads the real lua_scripts/Gauntlet/GauntletBoons.lua and GauntletData.lua under no-op ALE/AIO stubs
-- (nothing touches a server or DB) and writes every offerable entry (retired ids skipped). Use refresh.sh.
local root = assert(arg[1], "usage: export_catalog.lua <mod-turoran-roguelike dir> [commit]")
local commit = arg[2] or ""

local noop = function() end
AIO = { IsMainState = function() return true end, AddOnInit = noop, AddHandlers = function() return {} end,
    Handle = noop, Msg = function() return setmetatable({}, { __index = function() return noop end }) end }
for _, n in ipairs({ "RegisterPlayerEvent", "RegisterAllCreatureEvent", "RegisterCreatureEvent", "RegisterServerEvent",
    "RegisterGameObjectEvent", "RegisterMapEvent", "RegisterGroupEvent", "RegisterSpellEvent", "CreateLuaEvent",
    "RemoveEventById", "CharDBQuery", "CharDBExecute", "WorldDBQuery", "WorldDBExecute", "AuthDBQuery",
    "GetPlayersInWorld", "GetPlayerByGUID", "GetGameTime", "SendWorldMessage", "PrintError", "PrintInfo" }) do
    _G[n] = noop
end
-- Mobile Casting is only offered when the realm sets Roguelike.MobileCastingSpell (.101 sets 32427).
function GetConfigValue(key) if key == "Roguelike.MobileCastingSpell" then return "32427" end end
local realPrint = print
print = noop
dofile(root .. "/lua_scripts/Gauntlet/GauntletData.lua")
dofile(root .. "/lua_scripts/Gauntlet/GauntletBoons.lua")
print = realPrint
local B = assert(GauntletBoons, "GauntletBoons not defined")

local function esc(s)
    return (s:gsub('[%c"\\]', function(c)
        if c == '"' then return '\\"' elseif c == "\\" then return "\\\\" elseif c == "\n" then return "\\n" end
        return string.format("\\u%04x", c:byte())
    end))
end
local function enc(v)
    local t = type(v)
    if t == "nil" then return "null" elseif t == "boolean" then return tostring(v)
    elseif t == "number" then return (v == math.floor(v)) and string.format("%d", v) or tostring(v)
    elseif t == "string" then return '"' .. esc(v) .. '"' end
    if #v > 0 or next(v) == nil then
        local out = {}
        for i = 1, #v do out[i] = enc(v[i]) end
        return "[" .. table.concat(out, ",") .. "]"
    end
    local keys = {}
    for k in pairs(v) do keys[#keys + 1] = k end
    table.sort(keys)
    local out = {}
    for _, k in ipairs(keys) do out[#out + 1] = enc(tostring(k)) .. ":" .. enc(v[k]) end
    return "{" .. table.concat(out, ",") .. "}"
end

-- WoW colour codes: |cffRRGGBBtext|r. A red lead-in ("Lethal.") becomes a tag; the rest is plain text.
local function clean(desc)
    local tags = {}
    desc = desc:gsub("|c%x%x%x%x%x%x%x%x(.-)|r%s*", function(inner)
        tags[#tags + 1] = (inner:gsub("%.$", ""))
        return ""
    end)
    return (desc:gsub("|[cr]", "")), tags
end

local function export(kind, cat)
    local list = {}
    for id, e in pairs(cat) do
        if not e.retired and (not B.offerable or B.offerable(kind, id)) then
            local desc, tags = clean(e.desc or "")
            list[#list + 1] = { id = id, kind = kind, type = e.type, typeName = (B.TYPE_NAMES or {})[e.type] or e.type,
                cat = e.cat, name = e.name, icon = e.icon, desc = desc, tags = tags,
                goal = e.goal and { kind = e.goal.kind, n = e.goal.n } or nil, noWildCard = e.noWild or nil,
                requires = e.requires }
        end
    end
    table.sort(list, function(a, b) return a.id < b.id end)
    return list
end

local D = B.DIALS or {}
local dungeons = {}
for _, d in ipairs((GauntletData and GauntletData.DUNGEONS) or {}) do
    dungeons[#dungeons + 1] = { name = d.name, min = d.min, max = d.max, expansion = d.expansion }
end
io.write(enc({
    source = { repo = "Turor/mod-turoran-roguelike", branch = "feature/gauntlet-hero", commit = commit,
        file = "lua_scripts/Gauntlet/GauntletBoons.lua" },
    dials = { slotsMax = D.slotsMax, offers = D.offers, rerollsBase = D.rerollsBase,
        rerollsPerCurse = D.rerollsPerCurse, curseFloorPct = D.curseFloorPct },
    boons = export("boon", B.BOONS), curses = export("curse", B.CURSES), dungeons = dungeons,
}), "\n")
