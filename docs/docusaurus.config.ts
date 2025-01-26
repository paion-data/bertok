/**
 * Copyright 2024 Paion Data
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Bertok',
  tagline: 'Webservice with first-class support for Graph Database',
  favicon: 'img/favicon.ico',

  url: 'https://bertok.qubitpi.org',
  baseUrl: '/',

  organizationName: 'QubitPi',
  projectName: 'bertok',

  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          editUrl:
              'https://github.com/QubitPi/bertok/tree/master/docs',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    navbar: {
      title: 'Bertok',
      logo: {
        alt: 'Bertok Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: "localeDropdown",
          position: "left",
        },
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: "https://bertok.qubitpi.org/apidocs",
          label: "API",
          position: "left",
        },
        {
          href: 'https://github.com/QubitPi/bertok',
          label: ' ',
          position: 'right',
          className: 'header-icon-link header-github-link',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Documentation',
              to: '/docs/intro',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/QubitPi/bertok',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Paion Data. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ["java", "bash", "json"]
    },
    algolia: {
        appId: 'DXTOFNB5C6',
        apiKey: 'eca40f6bc63a92b106660dadd97f8703',
        indexName: 'aristotle_ws'
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
