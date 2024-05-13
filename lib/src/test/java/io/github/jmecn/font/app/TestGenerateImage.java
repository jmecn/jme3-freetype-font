package io.github.jmecn.font.app;

import com.jme3.texture.Image;
import io.github.jmecn.font.generator.FtBitmapFontData;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestGenerateImage {

    static final String FONT = "font/Noto_Serif_SC/NotoSerifSC-Regular.otf";
    static final String XIN = "guān zì zài pú sà 。\n" +
            "观 自 在 菩 萨 。\n" +
            "xíng shēn bō rě bō luó mì duō shí 。\n" +
            "行 深 般 若 波 罗 蜜 多 时 。\n" +
            "zhào jiàn wǔ yùn jiē kōng 。\n" +
            "照 见 五 蕴 皆 空。 \n" +
            "dù yī qiē kǔ è 。\n" +
            "度 一 切 苦 厄 。 \n" +
            "shě lì zǐ 。\n" +
            "舍 利 子。\n" +
            "sè bù yì kōng 。\n" +
            "色 不 异 空。\n" +
            "kōng bù yì sè 。\n" +
            "空 不 异 色 。 \n" +
            "sè jí shì kōng。\n" +
            "色 即 是 空。\n" +
            "kōng jí shì sè 。\n" +
            "空 即 是 色。\n" +
            "shòu xiǎng xíng shí 。\n" +
            "受 想 行 识。\n" +
            "yì fù rú shì 。\n" +
            "亦 复 如 是。\n" +
            "shě lì zǐ 。\n" +
            "舍 利 子。\n" +
            "shì zhū fǎ kōng xiāng 。\n" +
            "是 诸 法 空 相 。 \n" +
            "bù shēng bù miè 。\n" +
            "不 生 不 灭。\n" +
            "bù gòu bù jìng 。\n" +
            "不 垢 不 净。\n" +
            "bù zēng bù jiǎn 。\n" +
            "不 增 不 减 。 \n" +
            "shì gù kōng zhōng wú sè。\n" +
            "是 故 空 中 无 色。\n" +
            "wú shòu xiǎng xíng shí 。\n" +
            "无 受 想 行 识 。 \n" +
            "wú yǎn ěr bí shé shēn yì。\n" +
            "无 眼 耳 鼻 舌 身 意。\n" +
            "wú sè shēng xiāng wèi chù fǎ 。\n" +
            "无 色 声 香 味 触 法 。\n" +
            "wú yǎn jiè 。\n" +
            "无 眼 界。\n" +
            "nǎi zhì wú yì shi jie 。\n" +
            "乃 至 无 意 识 界 。 \n" +
            "wú wú míng 。\n" +
            "无 无 明。\n" +
            "yì wú wú míng jìn 。\n" +
            "亦 无 无 明 尽 。\n" +
            "nǎi zhì wú lǎo sǐ 。\n" +
            "乃 至 无 老 死。\n" +
            "yì wú lǎo sǐ jìn 。\n" +
            "亦 无 老 死 尽。 \n" +
            "wú kǔ jí miè dào。\n" +
            "无 苦 集 灭 道 。 \n" +
            "wú zhì yì wú dé 。\n" +
            "无 智 亦 无 得。\n" +
            "yǐ wú suǒ dé gù 。\n" +
            "以 无 所 得 故 。 \n" +
            "pú tí sà duǒ 。\n" +
            "菩提 萨 埵。\n" +
            "yī bō rě bō luó mì duō gù 。\n" +
            "依 般 若 波 罗 蜜 多 故 。 \n" +
            "xīn wú guà ài 。\n" +
            "心 无 挂 碍。\n" +
            "xīn wú guà ài 。\n" +
            "心 无 挂 碍。\n" +
            "wú guà ài gù 。\n" +
            "无 挂 碍 故。\n" +
            "wú yǒu kǒng bù 。\n" +
            "无 有 恐 怖。\n" +
            "yuǎn lí diān dǎo mèng xiǎng 。\n" +
            "远 离 颠 倒 梦 想。\n" +
            "jiū jìng niè pán 。\n" +
            "究 竟 涅槃。 \n" +
            "sān shì zhū fó 。\n" +
            "三 世 诸 佛。\n" +
            "yī bō rě bō luó mì duō gù 。\n" +
            "依 般 若 波 罗 蜜 多 故 。 \n" +
            "dé ā nòu duō luó sān miǎo sān pú tí 。\n" +
            "得 阿 耨 多 罗 三 藐 三 菩 提 。\n" +
            "gù zhī bō rě bō luó mì duō。\n" +
            "故 知 般 若 波 罗 蜜 多。\n" +
            "shì dà shén zhòu 。\n" +
            "是 大 神 咒 。 \n" +
            "shì dà míng zhòu 。\n" +
            "是 大 明 咒。\n" +
            "shì wú shàng zhòu 。\n" +
            "是 无 上 咒。 \n" +
            "shì wú děng děng zhòu 。\n" +
            "是 无 等 等 咒。\n" +
            "néng chú yī qiē kǔ 。\n" +
            "能 除 一 切 苦。\n" +
            "zhēn shí bù xū 。\n" +
            "真 实 不 虚 。 \n" +
            "gùshuōbō rě bō luó mì duō zhòu 。\n" +
            "故 说 般若 波 罗 蜜 多 咒。\n" +
            "jí shuō zhòu yuē 。\n" +
            "即 说 咒 曰 。 \n" +
            "jiē dì jiē dì 。\n" +
            "揭 谛 揭 谛。\n" +
            "bō luó jiē dì 。\n" +
            "波 罗 揭 谛 。\n" +
            "bō luó sēng jiē dì 。\n" +
            "波 罗 僧 揭 谛。\n" +
            "pú tí sà pó hē 。\n" +
            "菩 提 萨 婆 诃 。";
    static final String DASHIZHI = "大势至法王子。与其同伦。五十二菩萨。即从座起。顶礼佛足。而白佛言： ‘我忆往昔。恒河沙劫。有佛出世。名无量光。十二如来。相继一劫。其最后佛。名超日月光。 彼佛教我。念佛三昧。譬如有人。一专为忆。一人专忘。如是二人。若逢不逢。或见非见。二人相忆。二忆念深。 如是乃至。从生至生。同于形影。不相乖异。十方如来。怜念众生。如母忆子。若子逃逝。虽忆何为。子若忆母。如母忆时。母子历生。不相违远。若众生心。忆佛念佛。现前当来。必定见佛。去佛不远。不假方便。自得心开。如染香人。身有香气。此则名曰。香光庄严。我本因地。以念佛心。入无生忍。今于此界。摄念佛人。归于净土。佛问圆通。我无选择。都摄六根。净念相继。得三摩地。斯为第一。";
    public static void main(String[] args) throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT), 0)) {

            // use predefined packer
            Packer packer = new Packer(Image.Format.RGBA8, 512, 512, 1, false, new SkylineStrategy());

            FtFontParameter parameter = new FtFontParameter();
            parameter.packer = packer;
            parameter.size = 16;
            parameter.characters = FtFontParameter.DEFAULT_CHARS + XIN;
            parameter.incremental = true;

            FtBitmapFontData data = generator.generate(parameter);

            // add incremental font
            char[] chars = DASHIZHI.toCharArray();
            for (char ch : chars) {
                data.getGlyph(ch);
            }

            // show image
            Image[] images = parameter.getPacker().getPages().stream().map(Page::getImage).toArray(Image[]::new);
            TestDisplay.run(null, images);
        }
    }
}
