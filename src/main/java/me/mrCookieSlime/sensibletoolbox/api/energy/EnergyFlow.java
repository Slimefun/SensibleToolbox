/*
 * Copyright (C) 2014  Des Herriott
 *
 * This file is part of sensibletoolbox.
 *
 * Foobar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.mrCookieSlime.sensibletoolbox.api.energy;

/**
 * Represents the direction of energy (SCU) flow into or out
 * of a chargeable block.
 */
public enum EnergyFlow {
    /**
     * Energy flows into the block
     */
    IN,
    /**
     * Energy flows out of the block
     */
    OUT,
    /**
     * No energy flows in or out.
     */
    NONE;
}